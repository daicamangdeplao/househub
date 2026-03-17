package org.codenot.househub.service.knowledgemanagement.fileprocessor.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.codenot.househub.service.knowledgemanagement.fileprocessor.FileProcessorConstant;
import org.codenot.househub.service.knowledgemanagement.fileprocessor.utils.PdfChecker;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class DefaultParser implements FileParser {

    public String parse(Path path) {
        if (!PdfChecker.isPdf(path)) {
            return "";
        }

        try {
            Path resolvedPath = convertToTXT(path);
            return Files.readString(resolvedPath);
        } catch (IOException e) {
            log.error("Failed to convert PDF to TXT: [{}]", path, e);
        }

        return "";
    }

    private Path convertToTXT(Path path) throws IOException {
        String fileName = path.getFileName().toString();
        String baseName = fileName.contains(FileProcessorConstant.FILE_EXTENSION_SEPARATOR.getValue())
                ? fileName.substring(0, fileName.lastIndexOf(FileProcessorConstant.FILE_EXTENSION_SEPARATOR.getValue()))
                : fileName;

        String resolvedFilename = String.join(
                FileProcessorConstant.FILE_EXTENSION_SEPARATOR.getValue(),
                baseName,
                FileProcessorConstant.TXT_FILE_EXTENSION.getValue()
        );
        Path resolvedPath = path.resolveSibling(resolvedFilename);

        try (PDDocument pdDoc = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String parsedText = stripper.getText(pdDoc);
            Files.writeString(resolvedPath, parsedText);
            log.info("TXT file created successfully: [{}]", resolvedPath);
        } catch (IOException e) {
            log.error("Failed to convert PDF to TXT: [{}]", path, e);
            throw new RuntimeException(e);
        }

        return resolvedPath;
    }
}
