package com.resumeradar.controller;

import com.resumeradar.service.ResumeAnalysisService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume/optimized/download")
public class OptimizedResumeDownloadController {

	private static final MediaType DOCX_MEDIA_TYPE = MediaType.parseMediaType(
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document"
	);

	private final ResumeAnalysisService resumeAnalysisService;

	public OptimizedResumeDownloadController(ResumeAnalysisService resumeAnalysisService) {
		this.resumeAnalysisService = resumeAnalysisService;
	}

	@GetMapping("/pdf/{analysisId}")
	public ResponseEntity<byte[]> downloadPdf(
		@PathVariable Long analysisId,
		Authentication authentication
	) {
		byte[] document = resumeAnalysisService.downloadOptimizedResumePdf(analysisId, authentication.getName());

		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.header(HttpHeaders.CONTENT_DISPOSITION, attachment("optimized-resume.pdf"))
			.body(document);
	}

	@GetMapping("/docx/{analysisId}")
	public ResponseEntity<byte[]> downloadDocx(
		@PathVariable Long analysisId,
		Authentication authentication
	) {
		byte[] document = resumeAnalysisService.downloadOptimizedResumeDocx(analysisId, authentication.getName());

		return ResponseEntity.ok()
			.contentType(DOCX_MEDIA_TYPE)
			.header(HttpHeaders.CONTENT_DISPOSITION, attachment("optimized-resume.docx"))
			.body(document);
	}

	private String attachment(String filename) {
		return ContentDisposition.attachment()
			.filename(filename)
			.build()
			.toString();
	}
}
