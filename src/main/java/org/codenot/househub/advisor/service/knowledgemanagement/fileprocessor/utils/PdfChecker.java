package org.codenot.househub.advisor.service.knowledgemanagement.fileprocessor.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PdfChecker {
    private static final int MINIMAL_PDF_HEADER_LENGTH = 5;
    private static final String PDF_HEADER_PREFIX = "%PDF-";

    // The check is based on the fact: every compliant PDF begins with a header like:
    // %PDF-1.4
    // %PDF-1.7

    public static boolean isPdf(Path path) {

        if (!Files.isRegularFile(path)) {
            return false;
        }

        try (InputStream inputStream = Files.newInputStream(path)){
            byte[] bytes = inputStream.readNBytes(MINIMAL_PDF_HEADER_LENGTH);
            if (bytes.length < MINIMAL_PDF_HEADER_LENGTH) {
                return false;
            }

            String header = new String(bytes);
            return header.startsWith(PDF_HEADER_PREFIX);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
