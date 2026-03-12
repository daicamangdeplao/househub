package org.codenot.househub.service.knowledgemanagement.fileprocessor;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@Slf4j
public class FileParser {

    public Optional<File> parseFile(Path path) {
        if (!PdfChecker.isPdf(path)) {
            return Optional.of(path.toFile());
        }

        try {
            return Optional.of(convertToTXT(path));
        } catch (IOException e) {
            log.error("Failed to convert PDF to TXT: [{}]", path, e);
        }

        return Optional.empty();
    }

    private File convertToTXT(Path path) throws IOException {
        String fileName = path.getFileName().toString();
        String baseName = fileName.contains(FileProcessorConstant.FILE_EXTENSION_SEPARATOR.getValue())
                ? fileName.substring(0, fileName.lastIndexOf(FileProcessorConstant.FILE_EXTENSION_SEPARATOR.getValue()))
                : fileName;
        Path txtPath = path.resolveSibling(baseName + FileProcessorConstant.PROCESSING_FILE_EXTENSION.getValue());

        try (PDDocument pdDoc = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String parsedText = stripper.getText(pdDoc);
            Files.writeString(txtPath, parsedText);
            log.info("TXT file created successfully: [{}]", txtPath);
        } catch (IOException e) {
            log.error("Failed to convert PDF to TXT: [{}]", path, e);
            throw new RuntimeException(e);
        }

        return txtPath.toFile();
    }
}
