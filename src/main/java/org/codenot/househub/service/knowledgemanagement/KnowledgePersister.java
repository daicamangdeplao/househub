package org.codenot.househub.service.knowledgemanagement;

import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.entity.KnowledgeBaseJpaEntity;
import org.codenot.househub.repository.KnowledgeBaseRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KnowledgePersister {

    private final KnowledgeClassifier knowledgeClassifier;
    private final IdGeneratorService idGeneratorService;
    private final KnowledgeBaseRepository repository;
    private final EmbeddingModel embeddingModel;

    public KnowledgePersister(KnowledgeClassifier knowledgeClassifier, IdGeneratorService idGeneratorService, KnowledgeBaseRepository repository, EmbeddingModel embeddingModel) {
        this.knowledgeClassifier = knowledgeClassifier;
        this.idGeneratorService = idGeneratorService;
        this.repository = repository;
        this.embeddingModel = embeddingModel;
    }

    public void persistKnowledge(String knowledge) {
        UUID uuid = idGeneratorService.generateId();
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
                        .uuid(idGeneratorService.generateId())
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
