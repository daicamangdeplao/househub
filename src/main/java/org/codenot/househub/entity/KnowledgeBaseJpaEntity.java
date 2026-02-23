package org.codenot.househub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codenot.househub.service.knowledgemanagement.Topic;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "knowledge_base")
@Getter
@Setter
@NoArgsConstructor
public class KnowledgeBaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 128)
    private Topic topic;

    @Column(name = "published_year", nullable = false)
    private int publishedYear;

    /**
     * JPA only supports a small set of basic attribute types. A Java array like float[] is not a valid @Basic type, so the provider (Hibernate) fails during metadata validation with:
     * 'Basic' attribute type should not be 'float[]'
     * pgvector is a custom PostgreSQL type, and JPA does not know how to map it automatically.
     * <p>
     * refer: https://github.com/pgvector/pgvector-java
     */
    @Column(nullable = false, columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public KnowledgeBaseJpaEntity(UUID uuid, Topic topic, int i, float[] floats) {

    }
}
