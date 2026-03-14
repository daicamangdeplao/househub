package org.codenot.househub.service.knowledgemanagement;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.config.AppConfig;
import org.codenot.househub.service.knowledgemanagement.fileprocessor.FileProcessorConstant;
import org.codenot.househub.service.knowledgemanagement.uuidmanager.IdGenerator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
public class FileMover {

    private final AppConfig.ServiceProperties serviceProperties;
    private final IdGenerator idGenerator;

    public FileMover(AppConfig.ServiceProperties serviceProperties, IdGenerator idGenerator) {
        this.serviceProperties = serviceProperties;
        this.idGenerator = idGenerator;
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
    public void importKnowledgeBase() {
        log.info("Importing knowledge base from [{}] to [{}]", serviceProperties.sourceKnowledgebaseDirectory(), serviceProperties.targetKnowledgebaseDirectory());

        copyKnowledgeBase();
        cleanUpSourceDir();

        log.info("Knowledge base imported successfully");
    }

    public void archiveProcessedFiles(FileProcessorConstant param) {
        try (var paths = Files.walk(Path.of(serviceProperties.targetKnowledgebaseDirectory()))) {
            paths.forEach(source -> {
                // skip directories and symbolic links, only process regular files
                if (!Files.isRegularFile(source)) {
                    return;
                }

                Path fileName = source.getFileName();
                if (!fileName.endsWith(param.getValue())) {
                    return;
                }

                Path targetDir = switch (param) {
                    case PROCESSING_FILE_EXTENSION -> Path.of(serviceProperties.archiveKnowledgebaseDirectory());
                    case FILE_EXTENSION_SEPARATOR -> Path.of(serviceProperties.pdfArchiveKnowledgebaseDirectory());
                    default -> throw new IllegalArgumentException("Invalid FileProcessorConstant: " + param);
                };
                Path targetFile = targetDir.resolve(fileName);

                try {
                    Files.copy(source, targetFile, REPLACE_EXISTING);
                    log.info("Archived file [{}] to [{}]", source, targetFile);
                } catch (IOException e) {
                    log.error("Failed to archive file [{}] to [{}]", source, targetFile);
                }
            });
        } catch (IOException e) {
            log.error("Failed to archive files", e);
        }
    }

    private void copyKnowledgeBase() {
        try (var paths = Files.walk(Path.of(serviceProperties.sourceKnowledgebaseDirectory()))) {
            paths.forEach(source -> {
                // skip directories and symbolic links, only process regular files
                if (!Files.isRegularFile(source)) {
                    return;
                }
                String uuid = idGenerator.generateId().toString();
                Path target = Path.of(serviceProperties.targetKnowledgebaseDirectory()).resolve(uuid);

                try {
                    Files.copy(source, target, REPLACE_EXISTING);
                    log.info("Moved file [{}] to [{}]", source, target);
                } catch (IOException e) {
                    log.error("Failed to move file [{}] to [{}]", source, target);
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            log.error("Failed to move files to target directory [{}]", serviceProperties.targetKnowledgebaseDirectory(), e);
        }
    }

    private void cleanUpSourceDir() {
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
        } catch (IOException e) {
            log.error("Failed to delete files from source directory [{}]", serviceProperties.sourceKnowledgebaseDirectory(), e);
        }
    }

    @Service
    @Slf4j
    public static class KnowledgeClassifier {

        private static final String PROMPT = """
                    Use the following context to answer the question.
                    
                    Context:
                    {{context}}
                    
                    Question:
                    {{question}}
                    """;

        private final ChatModel chatModel;
        private final AppConfig.ServiceProperties serviceProperties;

        public KnowledgeClassifier(ChatModel chatModel, AppConfig.ServiceProperties serviceProperties) {
            this.chatModel = chatModel;
            this.serviceProperties = serviceProperties;
        }

        public Topic classifyTopic() {
            log.info("Classifying topic...");
            // 1) Retrieve context, the context should be collected from raw TXT file
            String context = extractIntroductionFromTxtFile(Path.of(serviceProperties.targetKnowledgebaseDirectory()));

            // 2) Build a prompt
            PromptTemplate template = PromptTemplate.from(PROMPT);
            String topics = Arrays.toString(Topic.values());
            String question = "What is the topic " +
                    topics.substring(1, topics.length() - 1) +
                    " of the following context? Only return the topic name, do not add any other information!";

            Prompt finalPrompt = template.apply(Map.of(
                    "context", context,
                    "question", question
            ));

            // 3) Call Claude
            String response = chatModel.chat(finalPrompt.text());
            Topic topic = Topic.fromString(response.toUpperCase());
            log.info("Topic: [{}]", topic);
            return topic;
        }

        private String extractIntroductionFromTxtFile(Path rawTxtFilePath) {

            // Mocked data, the real implementation should read the TXT file from the target directory
            return """
                    The project Babylon is a new OpenJDK project with the goal of enhancing Java reflection, allowing to reflect code from Java methods and Java lambdas, and being able to query their symbolic representation, called code models. These code models can be used at runtime to modify the code, perform optimizations, and/or perform code transformations to other programming models. Furthermore, code reflection allows Java developers to interact with foreign programming models and foreign programming languages without using any 3rd party libraries.
                    One of the foreign programming environments we are exploring in the project Babylon is the GPU environment through the CUDA and OpenCL programming models, called HAT ( Heterogeneous Accelerator Toolkit). The goal for HAT is to be able to offload and run efficient parallel workloads on hardware accelerators.
                    """;
        }
    }
}
