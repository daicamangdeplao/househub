package org.codenot.househub.advisor.service.knowledgemanagement;

import com.pgvector.PGvector;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.advisor.config.AppConfig;
import org.codenot.househub.advisor.entity.KnowledgeBaseJpaEntity;
import org.codenot.househub.advisor.repository.KnowledgeBaseRepository;
import org.codenot.househub.advisor.service.knowledgemanagement.fileprocessor.parser.DefaultParser;
import org.codenot.househub.advisor.service.knowledgemanagement.fileprocessor.parser.TikaParser;
import org.codenot.househub.advisor.service.knowledgemanagement.uuidmanager.IdGenerator;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
@Slf4j
public class Persister {

    private final FileMover.KnowledgeClassifier knowledgeClassifier;
    private final IdGenerator idGenerator;
    private final KnowledgeBaseRepository repository;
    private final EmbeddingModel embeddingModel;
    private final AppConfig.ServiceProperties serviceProperties;
    private final DefaultParser defaultParser;
    private final TikaParser tikaParser;

    public Persister(FileMover.KnowledgeClassifier knowledgeClassifier, IdGenerator idGenerator, KnowledgeBaseRepository repository, EmbeddingModel embeddingModel, AppConfig.ServiceProperties serviceProperties, DefaultParser defaultParser, TikaParser tikaParser) {
        this.knowledgeClassifier = knowledgeClassifier;
        this.idGenerator = idGenerator;
        this.repository = repository;
        this.embeddingModel = embeddingModel;
        this.serviceProperties = serviceProperties;
        this.defaultParser = defaultParser;
        this.tikaParser = tikaParser;
    }

    public void process() {
        try (Stream<Path> paths = Files.walk(Path.of(serviceProperties.targetKnowledgebaseDirectory()))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        // TODO diese Methode lauft ganz langsam. Die Asynchronous Technique kann hier verwendet werden.
                        String content = tikaParser.parse(path);
                        List<TextSegment> textSegments = splitIntoChunks(content);
                        for (TextSegment segment : textSegments) {
                            persistKnowledge(segment.toString());
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to persist knowledge", e);
        }
    }

    public void persistKnowledge(String knowledge) {
        UUID uuid = idGenerator.generateId();
        log.info("Embedding text: [{}]", knowledge);
        KnowledgeBaseJpaEntity entity = new KnowledgeBaseJpaEntity();
        entity.setTopic(Topic.JAVA); // TODO Only for test
        entity.setUuid(uuid);
        entity.setPublishedYear(LocalDateTime.now().getYear());
        // The embedding model is CPU-bound
        float[] embed = embeddingModel.embed(knowledge);
        entity.setEmbedding(new PGvector(embed));
        repository.save(entity);
        log.info("Persist knowledge with uuid [{}] successfully", uuid);
    }

    @Async("embeddingExecutor")
    public CompletableFuture<Void> persistKnowledgeAsync(String text) {
        return CompletableFuture.completedFuture(embedTextAsVector(text))
                .thenApply((embedding) -> KnowledgeBaseJpaEntity.builder()
                        .topic(Topic.JAVA) // TODO Only for test
                        .uuid(idGenerator.generateId())
                        .publishedYear(LocalDateTime.now().getYear())
                        .embedding(embedding)
                        .build())
                .thenAccept(e -> {
                    repository.save(e);
                    log.info("Persist knowledge with uuid [{}] successfully", e.getUuid());
                });
    }

    private PGvector embedTextAsVector(String text) {
        log.info("Embedding text: [{}]", text);
        return new PGvector(embeddingModel.embed(text));
    }

    private List<TextSegment> splitIntoChunks(String content) {
        Document document = Document.from(content);
        DocumentSplitter splitter = DocumentSplitters.recursive(
                // TODO dies muss im Config Klassen geholt werden, nicht hier
                600,   // chunk size
                120     // overlap
        );
        return splitter.split(document);
    }
}
