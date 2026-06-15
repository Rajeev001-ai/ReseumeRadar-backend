package com.resumeradar.dto;

import java.util.List;

public record AtsAnalysisResult(
	Integer atsScore,
	String overallFeedback,
	List<String> matchedKeywords,
	List<String> missingKeywords
) {
}
