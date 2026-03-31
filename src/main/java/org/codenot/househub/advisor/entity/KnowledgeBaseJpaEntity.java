package org.codenot.househub.advisor.entity;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;
import org.codenot.househub.advisor.service.knowledgemanagement.Topic;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "knowledge_base")
@Getter
@Setter
@Builder
@AllArgsConstructor
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
    @Column(name = "embedding", columnDefinition = "vector(768)", nullable = false)
    @Type(PGvectorType.class)
    private PGvector embedding;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}
