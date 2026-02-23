package org.codenot.househub.repository;

import org.codenot.househub.entity.KnowledgeBaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseJpaEntity, Long> {

    @Query(value = """
            SELECT *, embedding <-> :queryEmbedding AS distance 
            FROM knowledge_base
            ORDER BY distance ASC
            LIMIT 10
            """, nativeQuery = true)
    List<KnowledgeBaseJpaEntity> findMostSimilar(@Param("queryEmbedding") float[] queryEmbedding);
}
