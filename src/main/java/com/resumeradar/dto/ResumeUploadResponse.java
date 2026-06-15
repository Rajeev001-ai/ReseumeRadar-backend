package com.resumeradar.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ResumeUploadResponse {

	private Long analysisId;

	private String resumeFileName;

	private Integer atsScore;

	private String overallFeedback;

	private String aiSuggestions;

	private List<String> matchedKeywords;

	private List<String> missingKeywords;

	private List<ResumeChecklistItemResponse> checklist;

	private String extractedTextPreview;
}
