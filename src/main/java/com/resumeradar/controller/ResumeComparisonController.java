package com.resumeradar.controller;

import com.resumeradar.dto.ResumeComparisonResponse;
import com.resumeradar.service.ResumeAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume/compare")
public class ResumeComparisonController {

	private final ResumeAnalysisService resumeAnalysisService;

	public ResumeComparisonController(ResumeAnalysisService resumeAnalysisService) {
		this.resumeAnalysisService = resumeAnalysisService;
	}

	@GetMapping("/{oldAnalysisId}/{newAnalysisId}")
	public ResumeComparisonResponse compareResumeVersions(
		@PathVariable Long oldAnalysisId,
		@PathVariable Long newAnalysisId,
		Authentication authentication
	) {
		return resumeAnalysisService.compareAnalyses(oldAnalysisId, newAnalysisId, authentication.getName());
	}
}
