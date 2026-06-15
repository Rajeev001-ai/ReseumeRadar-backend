package com.resumeradar.service;

import com.resumeradar.dto.AdminAnalysisDetailResponse;
import com.resumeradar.dto.AdminAnalysisResponse;
import com.resumeradar.dto.AdminDashboardResponse;
import com.resumeradar.dto.AdminUserResponse;
import com.resumeradar.entity.MatchedKeyword;
import com.resumeradar.entity.MissingKeyword;
import com.resumeradar.entity.ResumeAnalysis;
import com.resumeradar.entity.User;
import com.resumeradar.exception.AnalysisNotFoundException;
import com.resumeradar.repository.ResumeAnalysisRepository;
import com.resumeradar.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

	private final ResumeAnalysisRepository resumeAnalysisRepository;

	private final UserRepository userRepository;

	private final AiSuggestionService aiSuggestionService;

	private final ResumeSectionChecklistService resumeSectionChecklistService;

	public AdminService(
		ResumeAnalysisRepository resumeAnalysisRepository,
		UserRepository userRepository,
		AiSuggestionService aiSuggestionService,
		ResumeSectionChecklistService resumeSectionChecklistService
	) {
		this.resumeAnalysisRepository = resumeAnalysisRepository;
		this.userRepository = userRepository;
		this.aiSuggestionService = aiSuggestionService;
		this.resumeSectionChecklistService = resumeSectionChecklistService;
	}

	@Transactional(readOnly = true)
	public AdminDashboardResponse getDashboard() {
		List<ResumeAnalysis> analyses = resumeAnalysisRepository.findAll();
		long totalAnalyses = analyses.size();
		double averageAtsScore = analyses.stream()
			.mapToInt(ResumeAnalysis::getAtsScore)
			.average()
			.orElse(0);

		return AdminDashboardResponse.builder()
			.totalUsers(userRepository.count())
			.totalAnalyses(totalAnalyses)
			.averageAtsScore(Math.round(averageAtsScore * 100.0) / 100.0)
			.excellentReportsCount(resumeAnalysisRepository.countByAtsScoreBetween(80, 100))
			.goodReportsCount(resumeAnalysisRepository.countByAtsScoreBetween(60, 79))
			.averageReportsCount(resumeAnalysisRepository.countByAtsScoreBetween(40, 59))
			.poorReportsCount(resumeAnalysisRepository.countByAtsScoreLessThan(40))
			.build();
	}

	@Transactional(readOnly = true)
	public List<AdminUserResponse> getUsers() {
		return userRepository.findAll()
			.stream()
			.map(this::toUserResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AdminAnalysisResponse> getAnalyses() {
		return resumeAnalysisRepository.findAllByOrderByCreatedAtDesc()
			.stream()
			.map(this::toAnalysisResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public AdminAnalysisDetailResponse getAnalysisDetail(Long analysisId) {
		if (analysisId == null || analysisId <= 0) {
			throw new AnalysisNotFoundException("Invalid analysis id");
		}

		ResumeAnalysis analysis = resumeAnalysisRepository.findById(analysisId)
			.orElseThrow(() -> new AnalysisNotFoundException("Analysis not found"));

		return toAnalysisDetailResponse(analysis);
	}

	private AdminUserResponse toUserResponse(User user) {
		return AdminUserResponse.builder()
			.userId(user.getId())
			.fullName(user.getFullName())
			.email(user.getEmail())
			.role(user.getRole())
			.createdAt(user.getCreatedAt())
			.totalAnalyses(resumeAnalysisRepository.countByUser(user))
			.build();
	}

	private AdminAnalysisResponse toAnalysisResponse(ResumeAnalysis analysis) {
		return AdminAnalysisResponse.builder()
			.analysisId(analysis.getId())
			.resumeFileName(analysis.getResumeFileName())
			.atsScore(analysis.getAtsScore())
			.overallFeedback(analysis.getOverallFeedback())
			.createdAt(analysis.getCreatedAt())
			.userFullName(analysis.getUser().getFullName())
			.userEmail(analysis.getUser().getEmail())
			.build();
	}

	private AdminAnalysisDetailResponse toAnalysisDetailResponse(ResumeAnalysis analysis) {
		return AdminAnalysisDetailResponse.builder()
			.analysisId(analysis.getId())
			.resumeFileName(analysis.getResumeFileName())
			.jobDescription(analysis.getJobDescription())
			.atsScore(analysis.getAtsScore())
			.overallFeedback(analysis.getOverallFeedback())
			.aiSuggestions(aiSuggestionService.resolveSuggestions(
				analysis.getAiSuggestions(),
				analysis.getAtsScore(),
				toMatchedKeywordList(analysis),
				toMissingKeywordList(analysis)
			))
			.matchedKeywords(toMatchedKeywordList(analysis))
			.missingKeywords(toMissingKeywordList(analysis))
			.checklist(resumeSectionChecklistService.buildChecklist(analysis, toMissingKeywordList(analysis)))
			.createdAt(analysis.getCreatedAt())
			.userFullName(analysis.getUser().getFullName())
			.userEmail(analysis.getUser().getEmail())
			.build();
	}

	private List<String> toMatchedKeywordList(ResumeAnalysis analysis) {
		return analysis.getMatchedKeywords()
			.stream()
			.map(MatchedKeyword::getKeyword)
			.toList();
	}

	private List<String> toMissingKeywordList(ResumeAnalysis analysis) {
		return analysis.getMissingKeywords()
			.stream()
			.map(MissingKeyword::getKeyword)
			.toList();
	}
}
