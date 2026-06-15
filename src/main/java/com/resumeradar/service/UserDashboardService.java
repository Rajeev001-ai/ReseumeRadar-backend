package com.resumeradar.service;

import com.resumeradar.dto.KeywordFrequencyResponse;
import com.resumeradar.dto.RecentAnalysisResponse;
import com.resumeradar.dto.UserDashboardAnalyticsResponse;
import com.resumeradar.dto.UserDashboardAnalyticsResponse.MonthlyAnalysisPoint;
import com.resumeradar.dto.UserDashboardAnalyticsResponse.ScoreTrendPoint;
import com.resumeradar.entity.MatchedKeyword;
import com.resumeradar.entity.MissingKeyword;
import com.resumeradar.entity.ResumeAnalysis;
import com.resumeradar.entity.User;
import com.resumeradar.exception.UserNotFoundException;
import com.resumeradar.repository.ResumeAnalysisRepository;
import com.resumeradar.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDashboardService {

	private final UserRepository userRepository;

	private final ResumeAnalysisRepository resumeAnalysisRepository;

	public UserDashboardService(
		UserRepository userRepository,
		ResumeAnalysisRepository resumeAnalysisRepository
	) {
		this.userRepository = userRepository;
		this.resumeAnalysisRepository = resumeAnalysisRepository;
	}

	@Transactional(readOnly = true)
	public UserDashboardAnalyticsResponse getAnalytics(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		List<ResumeAnalysis> analyses = resumeAnalysisRepository.findByUserOrderByCreatedAtDesc(user);
		List<ResumeAnalysis> chronologicalAnalyses = analyses.stream()
			.sorted(Comparator.comparing(ResumeAnalysis::getCreatedAt))
			.toList();

		long totalAnalyses = analyses.size();
		Integer bestScore = analyses.stream()
			.map(ResumeAnalysis::getAtsScore)
			.max(Integer::compareTo)
			.orElse(0);
		Double averageScore = totalAnalyses == 0
			? 0.0
			: BigDecimal.valueOf(analyses.stream().mapToInt(ResumeAnalysis::getAtsScore).average().orElse(0.0))
				.setScale(2, RoundingMode.HALF_UP)
				.doubleValue();
		Integer latestScore = analyses.stream()
			.findFirst()
			.map(ResumeAnalysis::getAtsScore)
			.orElse(0);
		long totalMatchedKeywords = analyses.stream()
			.mapToLong(analysis -> analysis.getMatchedKeywords().size())
			.sum();
		long totalMissingKeywords = analyses.stream()
			.mapToLong(analysis -> analysis.getMissingKeywords().size())
			.sum();
		Integer scoreImprovementTrend = chronologicalAnalyses.size() < 2
			? 0
			: chronologicalAnalyses.get(chronologicalAnalyses.size() - 1).getAtsScore()
				- chronologicalAnalyses.get(0).getAtsScore();

		List<KeywordFrequencyResponse> mostMissingKeywords = toTopKeywords(
			analyses,
			analysis -> analysis.getMissingKeywords().stream().map(MissingKeyword::getKeyword).toList()
		);

		return UserDashboardAnalyticsResponse.builder()
			.totalAnalyses(totalAnalyses)
			.totalResumeAnalyses(totalAnalyses)
			.bestAtsScore(bestScore)
			.averageAtsScore(averageScore)
			.latestAtsScore(latestScore)
			.totalMatchedKeywords(totalMatchedKeywords)
			.totalMissingKeywords(totalMissingKeywords)
			.mostCommonMissingKeywords(mostMissingKeywords)
			.recentAnalyses(toRecentAnalyses(analyses))
			.scoreImprovementTrend(scoreImprovementTrend)
			.scoreTrend(toScoreTrend(chronologicalAnalyses))
			.mostMissingSkills(mostMissingKeywords)
			.mostMatchedSkills(toTopKeywords(
				analyses,
				analysis -> analysis.getMatchedKeywords().stream().map(MatchedKeyword::getKeyword).toList()
			))
			.monthlyAnalysis(toMonthlyAnalysis(chronologicalAnalyses))
			.build();
	}

	private List<RecentAnalysisResponse> toRecentAnalyses(List<ResumeAnalysis> analyses) {
		return analyses.stream()
			.limit(5)
			.map(analysis -> RecentAnalysisResponse.builder()
				.analysisId(analysis.getId())
				.resumeFileName(analysis.getResumeFileName())
				.atsScore(analysis.getAtsScore())
				.overallFeedback(analysis.getOverallFeedback())
				.createdAt(analysis.getCreatedAt())
				.build())
			.toList();
	}

	private List<ScoreTrendPoint> toScoreTrend(List<ResumeAnalysis> analyses) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

		return analyses.stream()
			.map(analysis -> new ScoreTrendPoint(
				analysis.getId(),
				analysis.getResumeFileName(),
				analysis.getAtsScore(),
				analysis.getCreatedAt().format(formatter)
			))
			.toList();
	}

	private List<KeywordFrequencyResponse> toTopKeywords(
		List<ResumeAnalysis> analyses,
		Function<ResumeAnalysis, List<String>> keywordExtractor
	) {
		Map<String, Long> keywordCounts = new LinkedHashMap<>();

		for (ResumeAnalysis analysis : analyses) {
			for (String keyword : keywordExtractor.apply(analysis)) {
				keywordCounts.merge(keyword, 1L, Long::sum);
			}
		}

		return keywordCounts.entrySet()
			.stream()
			.sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
			.limit(8)
			.map(entry -> KeywordFrequencyResponse.builder()
				.keyword(entry.getKey())
				.count(entry.getValue())
				.build())
			.toList();
	}

	private List<MonthlyAnalysisPoint> toMonthlyAnalysis(List<ResumeAnalysis> chronologicalAnalyses) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
		Map<String, Long> monthlyCounts = chronologicalAnalyses.stream()
			.collect(Collectors.groupingBy(
				analysis -> analysis.getCreatedAt().format(formatter),
				LinkedHashMap::new,
				Collectors.counting()
			));

		return monthlyCounts.entrySet()
			.stream()
			.map(entry -> new MonthlyAnalysisPoint(entry.getKey(), entry.getValue()))
			.toList();
	}
}
