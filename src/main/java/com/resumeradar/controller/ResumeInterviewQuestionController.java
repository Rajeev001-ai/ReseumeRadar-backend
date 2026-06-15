package com.resumeradar.controller;

import com.resumeradar.dto.InterviewQuestionsResponse;
import com.resumeradar.service.ResumeAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume/interview-questions")
public class ResumeInterviewQuestionController {

	private final ResumeAnalysisService resumeAnalysisService;

	public ResumeInterviewQuestionController(ResumeAnalysisService resumeAnalysisService) {
		this.resumeAnalysisService = resumeAnalysisService;
	}

	@PostMapping("/{analysisId}")
	public InterviewQuestionsResponse generateInterviewQuestions(
		@PathVariable Long analysisId,
		Authentication authentication
	) {
		return resumeAnalysisService.generateInterviewQuestions(analysisId, authentication.getName());
	}
}
