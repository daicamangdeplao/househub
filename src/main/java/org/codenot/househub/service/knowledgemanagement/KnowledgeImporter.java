package org.codenot.househub.service.knowledgemanagement;

import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.config.AppConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
public class KnowledgeImporter {

    private final AppConfig.ServiceProperties serviceProperties;
    private final IdGeneratorService idGeneratorService;

    public KnowledgeImporter(AppConfig.ServiceProperties serviceProperties, IdGeneratorService idGeneratorService) {
        this.serviceProperties = serviceProperties;
        this.idGeneratorService = idGeneratorService;
    }

    /**
     * Imports the knowledge base by reading all files from the source directory,
     * assigning each a UUID-based filename, and copying them to the target directory.
     * <p>
     * read files from source directory
     * generate UUIDs
     * map UUIDs to file names
     * write files to target directory
     *
     * @throws IOException      if an I/O error occurs while walking the source directory
     * @throws RuntimeException if copying an individual file fails
     */
    public void importKnowledgeBase() throws IOException {
        log.info("Importing knowledge base from [{}] to [{}]", serviceProperties.sourceKnowledgebaseDirectory(), serviceProperties.targetKnowledgebaseDirectory());

        copyKnowledgeBase();
        cleanUpSourceDir();

        log.info("Knowledge base imported successfully");
    }

    private void copyKnowledgeBase() throws IOException {
        try (var paths = Files.walk(Path.of(serviceProperties.sourceKnowledgebaseDirectory()))) {
            paths.forEach(source -> {
                // skip directories and symbolic links, only process regular files
                if (!Files.isRegularFile(source)) {
                    return;
                }
                String uuid = idGeneratorService.generateId().toString();
                Path target = Path.of(serviceProperties.targetKnowledgebaseDirectory()).resolve(uuid);

                try {
                    Files.copy(source, target, REPLACE_EXISTING);
                    log.info("Moved file [{}] to [{}]", source, target);
                } catch (IOException e) {
                    log.error("Failed to move file [{}] to [{}]", source, target);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void cleanUpSourceDir() throws IOException {
        try (var paths = Files.walk(Path.of(serviceProperties.sourceKnowledgebaseDirectory()))) {
            paths.forEach(source -> {
                if (!Files.isRegularFile(source)) {
                    return;
                }
                try {
                    Files.delete(source);
                    log.info("Deleted file [{}]", source);
                } catch (IOException e) {
                    log.error("Failed to delete file [{}]", source);
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
