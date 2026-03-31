package org.codenot.househub.advisor.service.knowledgemanagement.fileprocessor.parser;

import java.nio.file.Path;

public interface FileParser {
    String parse(Path path);
}
