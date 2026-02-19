package org.codenot.househub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${service.importer.knowledgeBase.source.directory}")
    private String sourceKnowledgebaseDirectory;

    @Value("${service.importer.knowledgeBase.target.directory}")
    private String targetKnowledgebaseDirectory;

    @Bean
    public ServiceProperties serviceProperties() {
        return new ServiceProperties(
                sourceKnowledgebaseDirectory,
                targetKnowledgebaseDirectory
        );
    }

    public record ServiceProperties(
            String sourceKnowledgebaseDirectory,
            String targetKnowledgebaseDirectory
    ) {
    }
}
