package org.codenot.househub.service.knowledgemanagement;

import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.config.AppConfig;
import org.codenot.househub.entity.KnowledgeBaseJpaEntity;
import org.codenot.househub.repository.KnowledgeBaseRepository;
import org.codenot.househub.service.knowledgemanagement.uuidmanager.IdGenerator;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.codenot.househub.service.knowledgemanagement.fileprocessor.FileParser;
import org.codenot.househub.service.knowledgemanagement.fileprocessor.FileProcessorConstant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
@Slf4j
public class KnowledgePersister {

    private final FileMover.KnowledgeClassifier knowledgeClassifier;
    private final IdGenerator idGenerator;
    private final KnowledgeBaseRepository repository;
    private final EmbeddingModel embeddingModel;
    private final AppConfig.ServiceProperties serviceProperties;
    private final FileParser fileParser;

    public KnowledgePersister(FileMover.KnowledgeClassifier knowledgeClassifier, IdGenerator idGenerator, KnowledgeBaseRepository repository, EmbeddingModel embeddingModel, AppConfig.ServiceProperties serviceProperties, FileParser fileParser) {
        this.knowledgeClassifier = knowledgeClassifier;
        this.idGenerator = idGenerator;
        this.repository = repository;
        this.embeddingModel = embeddingModel;
        this.serviceProperties = serviceProperties;
        this.fileParser = fileParser;
    }

    public void persistKnowledge() {
        try (Stream<Path> paths = Files.walk(Path.of(serviceProperties.targetKnowledgebaseDirectory()))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> fileParser.parseFile(path).ifPresent(file -> {
                        try {
                            String content = Files.readString(file.toPath());
                            persistKnowledge(content);
                        } catch (IOException e) {
                            log.error("Failed to read file: [{}]", file, e);
                        }
                    }));
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
}
