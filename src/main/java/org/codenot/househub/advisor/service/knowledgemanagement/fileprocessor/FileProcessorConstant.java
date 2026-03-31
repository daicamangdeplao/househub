package org.codenot.househub.advisor.service.knowledgemanagement.fileprocessor;

public enum FileProcessorConstant {

    TXT_FILE_EXTENSION("txt"),
    PDF_FILE_EXTENSION("pdf"),
    FILE_EXTENSION_SEPARATOR(".");

    private final String value;

    FileProcessorConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
