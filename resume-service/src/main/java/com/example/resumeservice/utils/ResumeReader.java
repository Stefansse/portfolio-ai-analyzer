package com.example.resumeservice.utils;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;

public class ResumeReader {

    public static String extractText(InputStream fileStream, String filename) throws Exception {
        if (filename.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(fileStream);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                return extractor.getText();
            }
        } else if (filename.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(fileStream)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else {
            throw new RuntimeException("Unsupported file type for text extraction");
        }
    }
}
