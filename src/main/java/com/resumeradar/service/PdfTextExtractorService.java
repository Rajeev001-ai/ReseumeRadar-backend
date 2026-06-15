package com.resumeradar.service;

import com.resumeradar.exception.FileUploadException;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfTextExtractorService {

	public String extractText(MultipartFile file) {
		try (PDDocument document = Loader.loadPDF(file.getBytes())) {
			String text = new PDFTextStripper().getText(document);

			if (text == null || text.isBlank()) {
				throw new FileUploadException("Could not extract text from the PDF");
			}

			return text.trim();
		} catch (IOException exception) {
			throw new FileUploadException("Failed to read PDF file", exception);
		}
	}
}
