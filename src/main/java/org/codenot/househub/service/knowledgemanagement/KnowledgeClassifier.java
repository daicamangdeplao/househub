package org.codenot.househub.service.knowledgemanagement;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.codenot.househub.config.AppConfig;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

@Service
@Slf4j
public class KnowledgeClassifier {

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
