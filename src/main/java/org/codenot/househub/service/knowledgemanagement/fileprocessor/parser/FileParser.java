package org.codenot.househub.service.knowledgemanagement.fileprocessor.parser;

import java.nio.file.Path;

public interface FileParser {
    String parse(Path path);
}
