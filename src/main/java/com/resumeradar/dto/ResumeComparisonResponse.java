package com.resumeradar.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ResumeComparisonResponse(
	Integer previousScore,
	Integer currentScore,
	Integer scoreDifference,
	Double improvementPercentage,
	List<String> newlyAddedKeywords,
	List<String> removedKeywords,
	String recommendation
) {
}
