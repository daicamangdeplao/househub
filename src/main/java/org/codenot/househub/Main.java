package org.codenot.househub;

import dev.langchain4j.model.chat.ChatModel;
import org.codenot.househub.config.AppConfig;
import org.codenot.househub.service.knowledgemanagement.IdGeneratorService;
import org.codenot.househub.service.knowledgemanagement.Importer;
import org.codenot.househub.service.knowledgemanagement.TopicClassifier;
import org.codenot.househub.service.knowledgemanagement.UuidGeneratorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private final Importer importer;
    private final TopicClassifier topicClassifier;

    public Main(Importer importer, TopicClassifier topicClassifier) {
        this.importer = importer;
        this.topicClassifier = topicClassifier;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        importer.importKnowledgeBase();
        topicClassifier.classifyTopic();
    }
}
