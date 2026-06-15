package com.resumeradar.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AnalysisReportResponse {

	private Long analysisId;

	private String resumeFileName;

	private String jobDescription;

	private Integer atsScore;

	private String overallFeedback;

	private String aiSuggestions;

	private String interviewQuestions;

	private String optimizedResumeText;

	private List<String> matchedKeywords;

	private List<String> missingKeywords;

	private List<ResumeChecklistItemResponse> checklist;

	private LocalDateTime createdAt;
}
