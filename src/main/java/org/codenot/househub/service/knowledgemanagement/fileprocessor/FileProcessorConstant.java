package org.codenot.househub.service.knowledgemanagement.fileprocessor;

public enum FileProcessorConstant {

    PROCESSING_FILE_EXTENSION(".txt"),
    FILE_EXTENSION_SEPARATOR(".");

    private final String value;

    FileProcessorConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
