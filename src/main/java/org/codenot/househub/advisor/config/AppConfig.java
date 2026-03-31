package org.codenot.househub.advisor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${service.importer.knowledgeBase.source.directory}")
    private String sourceKnowledgebaseDirectory;

    @Value("${service.importer.knowledgeBase.target.directory}")
    private String targetKnowledgebaseDirectory;

    @Value("${service.importer.knowledgeBase.archive.directory}")
    private String archiveKnowledgebaseDirectory;

    @Value("${service.importer.knowledgeBase.pdfArchive.directory}")
    private String pdfArchiveKnowledgebaseDirectory;

    @Bean
    public ServiceProperties serviceProperties() {
        return new ServiceProperties(
                sourceKnowledgebaseDirectory,
                targetKnowledgebaseDirectory,
                archiveKnowledgebaseDirectory,
                pdfArchiveKnowledgebaseDirectory
        );
    }

    public record ServiceProperties(
            String sourceKnowledgebaseDirectory,
            String targetKnowledgebaseDirectory,
            String archiveKnowledgebaseDirectory,
            String pdfArchiveKnowledgebaseDirectory
    ) {
    }
}
