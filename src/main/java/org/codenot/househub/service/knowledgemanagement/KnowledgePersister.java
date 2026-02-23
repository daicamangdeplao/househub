package org.codenot.househub.service.knowledgemanagement;

import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.entity.KnowledgeBaseJpaEntity;
import org.codenot.househub.repository.KnowledgeBaseRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public boolean persistKnowledge(String knowledge) {
//        Topic topic = knowledgeClassifier.classifyTopic();

        KnowledgeBaseJpaEntity entity = new KnowledgeBaseJpaEntity();
        entity.setTopic(Topic.JAVA); // TODO Only for test
        entity.setUuid(idGeneratorService.generateId());
        entity.setPublishedYear(LocalDateTime.now().getYear());
        // TODO this step takes too long. Concurrently persist knowledge base to DB
        float[] embed = embeddingModel.embed(knowledge);
        entity.setEmbedding(new PGvector(embed));

        repository.save(entity);
        return false;
    }
}
