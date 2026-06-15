package com.resumeradar.service;

import com.resumeradar.entity.ResumeAnalysis;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class OptimizedResumeDocumentService {

	private static final float MARGIN = 54;

	private static final float LINE_HEIGHT = 15;

	private final PDFont regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

	private final PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

	public byte[] generatePdf(ResumeAnalysis analysis) {
		try (PDDocument document = new PDDocument();
			 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfWriter writer = new PdfWriter(document);
			writer.writeResume(analysis.getOptimizedResumeText());
			writer.close();
			document.save(outputStream);
			return outputStream.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to generate optimized resume PDF", exception);
		}
	}

	public byte[] generateDocx(ResumeAnalysis analysis) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
			writeZipEntry(zipOutputStream, "[Content_Types].xml", contentTypesXml());
			writeZipEntry(zipOutputStream, "_rels/.rels", relationshipsXml());
			writeZipEntry(zipOutputStream, "word/document.xml", documentXml(analysis.getOptimizedResumeText()));
			writeZipEntry(zipOutputStream, "word/styles.xml", stylesXml());
			zipOutputStream.finish();
			return outputStream.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to generate optimized resume DOCX", exception);
		}
	}

	private void writeZipEntry(ZipOutputStream zipOutputStream, String name, String content) throws IOException {
		zipOutputStream.putNextEntry(new ZipEntry(name));
		zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
		zipOutputStream.closeEntry();
	}

	private String documentXml(String resumeText) {
		StringBuilder body = new StringBuilder();

		for (String rawLine : normalizeLines(resumeText)) {
			String line = rawLine.trim();
			if (line.isBlank()) {
				body.append(paragraph("", "Normal"));
			} else if (isHeading(line)) {
				body.append(paragraph(cleanHeading(line), "Heading1"));
			} else if (isBullet(line)) {
				body.append(paragraph("- " + cleanBullet(line), "Bullet"));
			} else {
				body.append(paragraph(line, "Normal"));
			}
		}

		return """
			<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
			<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
			  <w:body>
			%s
			    <w:sectPr>
			      <w:pgSz w:w="12240" w:h="15840"/>
			      <w:pgMar w:top="720" w:right="720" w:bottom="720" w:left="720"/>
			    </w:sectPr>
			  </w:body>
			</w:document>
			""".formatted(body);
	}

	private String paragraph(String text, String styleId) {
		String style = styleId == null || styleId.isBlank()
			? ""
			: "<w:pPr><w:pStyle w:val=\"" + styleId + "\"/></w:pPr>";

		return """
			    <w:p>
			      %s
			      <w:r><w:t xml:space="preserve">%s</w:t></w:r>
			    </w:p>
			""".formatted(style, escapeXml(text));
	}

	private String contentTypesXml() {
		return """
			<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
			<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
			  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
			  <Default Extension="xml" ContentType="application/xml"/>
			  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
			  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
			</Types>
			""";
	}

	private String relationshipsXml() {
		return """
			<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
			  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
			</Relationships>
			""";
	}

	private String stylesXml() {
		return """
			<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
			<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
			  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
			    <w:name w:val="Normal"/>
			    <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/></w:rPr>
			  </w:style>
			  <w:style w:type="paragraph" w:styleId="Heading1">
			    <w:name w:val="Heading 1"/>
			    <w:basedOn w:val="Normal"/>
			    <w:pPr><w:spacing w:before="220" w:after="80"/></w:pPr>
			    <w:rPr><w:b/><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="28"/></w:rPr>
			  </w:style>
			  <w:style w:type="paragraph" w:styleId="Bullet">
			    <w:name w:val="ATS Bullet"/>
			    <w:basedOn w:val="Normal"/>
			    <w:pPr><w:ind w:left="360" w:hanging="180"/></w:pPr>
			    <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/></w:rPr>
			  </w:style>
			</w:styles>
			""";
	}

	private List<String> normalizeLines(String text) {
		if (text == null || text.isBlank()) {
			return List.of("");
		}

		return text.replace("\r", "").lines().toList();
	}

	private boolean isHeading(String line) {
		String cleanLine = cleanHeading(line);
		return !isBullet(line)
			&& cleanLine.length() <= 48
			&& (line.endsWith(":") || cleanLine.equals(cleanLine.toUpperCase()));
	}

	private String cleanHeading(String line) {
		return line.replaceAll("^#+\\s*", "")
			.replaceAll("\\*\\*", "")
			.replaceAll(":$", "")
			.trim();
	}

	private boolean isBullet(String line) {
		return line.matches("^[-*•]\\s+.*") || line.matches("^\\d+[.)]\\s+.*");
	}

	private String cleanBullet(String line) {
		return line.replaceAll("^[-*•]\\s+", "")
			.replaceAll("^\\d+[.)]\\s+", "")
			.trim();
	}

	private String escapeXml(String text) {
		return text.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&apos;");
	}

	private class PdfWriter {

		private final PDDocument document;

		private PDPage page;

		private PDPageContentStream contentStream;

		private float y;

		PdfWriter(PDDocument document) throws IOException {
			this.document = document;
			addPage();
		}

		void writeResume(String resumeText) throws IOException {
			writeText("Optimized Resume", boldFont, 18, MARGIN, y);
			y -= 14;
			writeText("Generated by ResumeRadar on " + LocalDate.now(), regularFont, 9, MARGIN, y);
			y -= 28;

			for (String rawLine : normalizeLines(resumeText)) {
				String line = rawLine.trim();
				if (line.isBlank()) {
					y -= 8;
				} else if (isHeading(line)) {
					writeHeading(cleanHeading(line));
				} else if (isBullet(line)) {
					writeWrappedText("- " + cleanBullet(line), regularFont, 10.5f, MARGIN + 12, getContentWidth() - 12);
				} else {
					writeWrappedText(line, regularFont, 10.5f, MARGIN, getContentWidth());
				}
			}
		}

		void close() throws IOException {
			if (contentStream != null) {
				contentStream.close();
			}
		}

		private void writeHeading(String heading) throws IOException {
			ensureSpace(36);
			y -= 8;
			writeText(heading, boldFont, 13, MARGIN, y);
			y -= 7;
			contentStream.moveTo(MARGIN, y);
			contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, y);
			contentStream.stroke();
			y -= 18;
		}

		private void writeWrappedText(String text, PDFont font, float fontSize, float x, float maxWidth) throws IOException {
			for (String line : wrapText(text, font, fontSize, maxWidth)) {
				ensureSpace(24);
				writeText(line, font, fontSize, x, y);
				y -= LINE_HEIGHT;
			}
			y -= 2;
		}

		private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
			if (text == null || text.isBlank()) {
				return List.of("");
			}

			java.util.ArrayList<String> lines = new java.util.ArrayList<>();
			StringBuilder line = new StringBuilder();

			for (String word : text.split("\\s+")) {
				String candidate = line.isEmpty() ? word : line + " " + word;
				if (font.getStringWidth(sanitize(candidate)) / 1000 * fontSize <= maxWidth) {
					line = new StringBuilder(candidate);
				} else {
					if (!line.isEmpty()) {
						lines.add(line.toString());
					}
					line = new StringBuilder(word);
				}
			}

			if (!line.isEmpty()) {
				lines.add(line.toString());
			}

			return lines;
		}

		private void ensureSpace(float requiredSpace) throws IOException {
			if (y - requiredSpace < MARGIN) {
				addPage();
			}
		}

		private void addPage() throws IOException {
			if (contentStream != null) {
				contentStream.close();
			}

			page = new PDPage(PDRectangle.LETTER);
			document.addPage(page);
			contentStream = new PDPageContentStream(document, page);
			y = page.getMediaBox().getHeight() - MARGIN;
		}

		private void writeText(String text, PDFont font, float fontSize, float x, float textY) throws IOException {
			contentStream.beginText();
			contentStream.setFont(font, fontSize);
			contentStream.newLineAtOffset(x, textY);
			contentStream.showText(sanitize(text));
			contentStream.endText();
		}

		private float getContentWidth() {
			return page.getMediaBox().getWidth() - (MARGIN * 2);
		}

		private String sanitize(String text) {
			return (text == null ? "" : text)
				.replace("\t", " ")
				.replaceAll("[^\\x20-\\x7E]", "");
		}
	}
}
