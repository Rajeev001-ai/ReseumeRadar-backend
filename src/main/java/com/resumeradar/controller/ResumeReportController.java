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
@RequestMapping("/api/resume/report")
public class ResumeReportController {

	private final ResumeAnalysisService resumeAnalysisService;

	public ResumeReportController(ResumeAnalysisService resumeAnalysisService) {
		this.resumeAnalysisService = resumeAnalysisService;
	}

	@GetMapping("/download/{analysisId}")
	public ResponseEntity<byte[]> downloadReport(
		@PathVariable Long analysisId,
		Authentication authentication
	) {
		byte[] report = resumeAnalysisService.downloadPdfReport(analysisId, authentication.getName());

		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.header(
				HttpHeaders.CONTENT_DISPOSITION,
				ContentDisposition.attachment()
					.filename("resumeradar-report-" + analysisId + ".pdf")
					.build()
					.toString()
			)
			.body(report);
	}
}
