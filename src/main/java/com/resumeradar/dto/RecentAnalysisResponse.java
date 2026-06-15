package com.resumeradar.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RecentAnalysisResponse(
	Long analysisId,
	String resumeFileName,
	Integer atsScore,
	String overallFeedback,
	LocalDateTime createdAt
) {
}
