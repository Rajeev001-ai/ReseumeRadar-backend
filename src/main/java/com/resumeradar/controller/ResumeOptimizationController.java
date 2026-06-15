package com.resumeradar.controller;

import com.resumeradar.dto.OptimizeResumeResponse;
import com.resumeradar.service.ResumeAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume")
public class ResumeOptimizationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResumeOptimizationController.class);

	private final ResumeAnalysisService resumeAnalysisService;

	public ResumeOptimizationController(ResumeAnalysisService resumeAnalysisService) {
		this.resumeAnalysisService = resumeAnalysisService;
	}

	@PostMapping("/optimize/{analysisId}")
	public OptimizeResumeResponse optimizeResume(
		@PathVariable Long analysisId,
		Authentication authentication
	) {
		LOGGER.info("Resume optimization API call started for analysisId={} user={}", analysisId, authentication.getName());
		return resumeAnalysisService.optimizeResume(analysisId, authentication.getName());
	}
}
