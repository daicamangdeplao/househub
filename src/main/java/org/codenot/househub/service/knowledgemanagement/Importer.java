package org.codenot.househub.service.knowledgemanagement;

import org.codenot.househub.service.knowledgemanagement.fileprocessor.FileProcessorConstant;
import org.springframework.stereotype.Service;

@Service
public class Importer {

    private final FileMover fileMover;
    private final Persister persister;

    public Importer(FileMover fileMover, Persister persister) {
        this.fileMover = fileMover;
        this.persister = persister;
    }

    public void run() {
        fileMover.moveDocsToTarget();
        persister.process();
        fileMover.archiveProcessedFiles(FileProcessorConstant.TXT_FILE_EXTENSION);
        fileMover.archiveProcessedFiles(FileProcessorConstant.PDF_FILE_EXTENSION);
    }
}

/**
 *         // Asynchronous execution
 * //        Instant now = Instant.now();
 * //        CompletableFuture[] futures = List.of(TEXT_1, TEXT_2, TEXT_3, TEXT_4)
 * //                .parallelStream()
 * //                .map(knowledgePersister::persistKnowledgeAsync)
 * //                .toArray(CompletableFuture[]::new);
 * //        try {
 * //            CompletableFuture.allOf(futures).join();
 * //        } catch (CancellationException cancellationException) {
 * //            log.error("Asynchronous execution was cancelled", cancellationException);
 * //        } catch (CompletionException completionException) {
 * //            log.error("Error occurred during asynchronous execution", completionException);
 * //        }
 * //        Duration duration = Duration.between(now, Instant.now());
 * //        log.info("Total time: [{}] seconds", duration.getSeconds());
 *
 *         // Synchronous execution
 * //        Instant now = Instant.now();
 * //        Stream.of(TEXT_1, TEXT_2, TEXT_3, TEXT_4)
 * //                .forEach(knowledgePersister::persistKnowledge);
 * //        Duration duration = Duration.between(now, Instant.now());
 * //        log.info("Total time: [{}] seconds", duration.getSeconds());
 *
 * */
