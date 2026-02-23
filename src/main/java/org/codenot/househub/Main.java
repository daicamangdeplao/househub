package org.codenot.househub;

import org.codenot.househub.service.knowledgemanagement.KnowledgeImporter;
import org.codenot.househub.service.knowledgemanagement.KnowledgeClassifier;
import org.codenot.househub.service.knowledgemanagement.KnowledgePersister;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private final KnowledgeImporter knowledgeImporter;
    private final KnowledgeClassifier knowledgeClassifier;
    private final KnowledgePersister knowledgePersister;

    public Main(KnowledgeImporter knowledgeImporter, KnowledgeClassifier knowledgeClassifier, KnowledgePersister knowledgePersister) {
        this.knowledgeImporter = knowledgeImporter;
        this.knowledgeClassifier = knowledgeClassifier;
        this.knowledgePersister = knowledgePersister;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        importer.importKnowledgeBase();
//        knowledgeClassifier.classifyTopic();
        knowledgePersister.persistKnowledge("Hello world!");
    }
}
