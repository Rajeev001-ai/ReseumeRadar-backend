package com.resumeradar.controller;

import com.resumeradar.dto.AnalysisHistoryResponse;
import com.resumeradar.dto.AnalysisReportResponse;
import com.resumeradar.dto.ResumeUploadResponse;
import com.resumeradar.service.ResumeAnalysisService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/resume/analyze", "/api/resume/analysis"})
public class ResumeAnalysisController {

	private final ResumeAnalysisService resumeAnalysisService;

	public ResumeAnalysisController(ResumeAnalysisService resumeAnalysisService) {
		this.resumeAnalysisService = resumeAnalysisService;
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResumeUploadResponse uploadResume(
		@RequestParam("file") MultipartFile file,
		@RequestParam("jobDescription") String jobDescription,
		Authentication authentication
	) {
		return resumeAnalysisService.uploadAndCreateAnalysis(file, jobDescription, authentication.getName());
	}

	@GetMapping("/history")
	public List<AnalysisHistoryResponse> getAnalysisHistory(Authentication authentication) {
		return resumeAnalysisService.getAnalysisHistory(authentication.getName());
	}

	@GetMapping("/{analysisId}")
	public AnalysisReportResponse getAnalysisReport(
		@PathVariable Long analysisId,
		Authentication authentication
	) {
		return resumeAnalysisService.getAnalysisReport(analysisId, authentication.getName());
	}
}
