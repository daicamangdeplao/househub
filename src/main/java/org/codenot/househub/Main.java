package org.codenot.househub;

import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.service.knowledgemanagement.KnowledgeClassifier;
import org.codenot.househub.service.knowledgemanagement.KnowledgeImporter;
import org.codenot.househub.service.knowledgemanagement.KnowledgePersister;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class Main implements CommandLineRunner {

    @Value("${TEXT_1}")
    private String TEXT_1;
    @Value("${TEXT_2}")
    private String TEXT_2;
    @Value("${TEXT_3}")
    private String TEXT_3;
    @Value("${TEXT_4}")
    private String TEXT_4;

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
    public void run(String... args) {

        // Asynchronous execution
//        Instant now = Instant.now();
//        CompletableFuture[] futures = List.of(TEXT_1, TEXT_2, TEXT_3, TEXT_4)
//                .parallelStream()
//                .map(knowledgePersister::persistKnowledgeAsync)
//                .toArray(CompletableFuture[]::new);
//        try {
//            CompletableFuture.allOf(futures).join();
//        } catch (CancellationException cancellationException) {
//            log.error("Asynchronous execution was cancelled", cancellationException);
//        } catch (CompletionException completionException) {
//            log.error("Error occurred during asynchronous execution", completionException);
//        }
//        Duration duration = Duration.between(now, Instant.now());
//        log.info("Total time: [{}] seconds", duration.getSeconds());

        // Synchronous execution
        Instant now = Instant.now();
        Stream.of(TEXT_1, TEXT_2, TEXT_3, TEXT_4)
                .forEach(knowledgePersister::persistKnowledge);
        Duration duration = Duration.between(now, Instant.now());
        log.info("Total time: [{}] seconds", duration.getSeconds());
    }
}
