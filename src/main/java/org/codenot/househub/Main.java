package org.codenot.househub;

import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.service.knowledgemanagement.FileMover;
import org.codenot.househub.service.knowledgemanagement.Persister;
import org.codenot.househub.service.knowledgemanagement.fileprocessor.FileParser;
import org.codenot.househub.service.knowledgemanagement.fileprocessor.FileProcessorConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

    private final FileMover fileMover;
    private final FileMover.KnowledgeClassifier knowledgeClassifier;
    private final Persister persister;
    private final FileParser fileParser;

    public Main(FileMover fileMover, FileMover.KnowledgeClassifier knowledgeClassifier, Persister persister, FileParser fileParser) {
        this.fileMover = fileMover;
        this.knowledgeClassifier = knowledgeClassifier;
        this.persister = persister;
        this.fileParser = fileParser;
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
//        Instant now = Instant.now();
//        Stream.of(TEXT_1, TEXT_2, TEXT_3, TEXT_4)
//                .forEach(knowledgePersister::persistKnowledge);
//        Duration duration = Duration.between(now, Instant.now());
//        log.info("Total time: [{}] seconds", duration.getSeconds());

        // Test parse file
//        fileParser.parseFile(Path.of("C:\\workspace\\knowledge-base\\source\\test.pdf"));
//        fileMover.archiveProcessedFiles(FileProcessorConstant.PROCESSING_FILE_EXTENSION);
//        fileMover.archiveProcessedFiles(FileProcessorConstant.PDF_FILE_EXTENSION);

        // Test complete process
        fileMover.importKnowledgeBase();
        persister.persistKnowledge();
        fileMover.archiveProcessedFiles(FileProcessorConstant.PROCESSING_FILE_EXTENSION);
        fileMover.archiveProcessedFiles(FileProcessorConstant.PDF_FILE_EXTENSION);


        System.out.println();
    }
}
