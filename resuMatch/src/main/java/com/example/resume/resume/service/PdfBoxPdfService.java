package com.example.resume.resume.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service // ⬅️ 빈 등록
public class PdfBoxPdfService implements PdfService {
    @Override
    public String extractText(InputStream in) {
        try (PDDocument doc = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        } catch (Exception e) {
            log.error("PDF 텍스트 추출 실패", e);
            return "";
        }
    }
}
