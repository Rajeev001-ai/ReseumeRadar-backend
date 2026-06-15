package com.resumeradar.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record UserDashboardAnalyticsResponse(
	long totalAnalyses,
	long totalResumeAnalyses,
	Integer bestAtsScore,
	Double averageAtsScore,
	Integer latestAtsScore,
	long totalMatchedKeywords,
	long totalMissingKeywords,
	List<KeywordFrequencyResponse> mostCommonMissingKeywords,
	List<RecentAnalysisResponse> recentAnalyses,
	Integer scoreImprovementTrend,
	List<ScoreTrendPoint> scoreTrend,
	List<KeywordFrequencyResponse> mostMissingSkills,
	List<KeywordFrequencyResponse> mostMatchedSkills,
	List<MonthlyAnalysisPoint> monthlyAnalysis
) {

	public record ScoreTrendPoint(
		Long analysisId,
		String resumeFileName,
		Integer atsScore,
		String analysisDate
	) {
	}

	public record MonthlyAnalysisPoint(
		String month,
		long count
	) {
	}
}
