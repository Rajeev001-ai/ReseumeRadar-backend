package com.resumeradar.service;

import com.resumeradar.dto.AnalysisHistoryResponse;
import com.resumeradar.dto.AnalysisReportResponse;
import com.resumeradar.dto.AtsAnalysisResult;
import com.resumeradar.dto.InterviewQuestionsResponse;
import com.resumeradar.dto.OptimizeResumeResponse;
import com.resumeradar.dto.ResumeComparisonResponse;
import com.resumeradar.dto.ResumeUploadResponse;
import com.resumeradar.entity.MatchedKeyword;
import com.resumeradar.entity.MissingKeyword;
import com.resumeradar.entity.Role;
import com.resumeradar.entity.ResumeAnalysis;
import com.resumeradar.entity.User;
import com.resumeradar.exception.AnalysisNotFoundException;
import com.resumeradar.exception.FileUploadException;
import com.resumeradar.exception.UnauthorizedAccessException;
import com.resumeradar.exception.UserNotFoundException;
import com.resumeradar.repository.ResumeAnalysisRepository;
import com.resumeradar.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeAnalysisService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResumeAnalysisService.class);

	private static final String PDF_CONTENT_TYPE = "application/pdf";

	private static final int PREVIEW_LENGTH = 500;

	private final long maxFileSizeBytes;

	private final PdfTextExtractorService pdfTextExtractorService;

	private final ResumeAnalysisRepository resumeAnalysisRepository;

	private final UserRepository userRepository;

	private final AtsAnalyzerService atsAnalyzerService;

	private final AiSuggestionService aiSuggestionService;

	private final ResumeSectionChecklistService resumeSectionChecklistService;

	private final InterviewQuestionService interviewQuestionService;

	private final ResumeOptimizationService resumeOptimizationService;

	private final OptimizedResumeDocumentService optimizedResumeDocumentService;

	private final PdfReportService pdfReportService;

	public ResumeAnalysisService(
		@Value("${app.resume.max-file-size-bytes}") long maxFileSizeBytes,
		PdfTextExtractorService pdfTextExtractorService,
		ResumeAnalysisRepository resumeAnalysisRepository,
		UserRepository userRepository,
		AtsAnalyzerService atsAnalyzerService,
		AiSuggestionService aiSuggestionService,
		ResumeSectionChecklistService resumeSectionChecklistService,
		InterviewQuestionService interviewQuestionService,
		ResumeOptimizationService resumeOptimizationService,
		OptimizedResumeDocumentService optimizedResumeDocumentService,
		PdfReportService pdfReportService
	) {
		this.maxFileSizeBytes = maxFileSizeBytes;
		this.pdfTextExtractorService = pdfTextExtractorService;
		this.resumeAnalysisRepository = resumeAnalysisRepository;
		this.userRepository = userRepository;
		this.atsAnalyzerService = atsAnalyzerService;
		this.aiSuggestionService = aiSuggestionService;
		this.resumeSectionChecklistService = resumeSectionChecklistService;
		this.interviewQuestionService = interviewQuestionService;
		this.resumeOptimizationService = resumeOptimizationService;
		this.optimizedResumeDocumentService = optimizedResumeDocumentService;
		this.pdfReportService = pdfReportService;
	}

	public ResumeUploadResponse uploadAndCreateAnalysis(
		MultipartFile file,
		String jobDescription,
		String userEmail
	) {
		validateUpload(file, jobDescription);

		User user = userRepository.findByEmail(userEmail)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		String resumeText = pdfTextExtractorService.extractText(file);
		AtsAnalysisResult atsAnalysisResult = atsAnalyzerService.analyze(resumeText, jobDescription);
		String aiSuggestions = aiSuggestionService.generateSuggestions(
			resumeText,
			jobDescription,
			atsAnalysisResult.atsScore(),
			atsAnalysisResult.matchedKeywords(),
			atsAnalysisResult.missingKeywords()
		);

		ResumeAnalysis analysis = ResumeAnalysis.builder()
			.user(user)
			.resumeFileName(file.getOriginalFilename())
			.resumeText(resumeText)
			.jobDescription(jobDescription.trim())
			.atsScore(atsAnalysisResult.atsScore())
			.overallFeedback(atsAnalysisResult.overallFeedback())
			.aiSuggestions(aiSuggestions)
			.build();

		resumeSectionChecklistService.detectSections(analysis);
		addMatchedKeywords(analysis, atsAnalysisResult.matchedKeywords());
		addMissingKeywords(analysis, atsAnalysisResult.missingKeywords());

		ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);

		return ResumeUploadResponse.builder()
			.analysisId(savedAnalysis.getId())
			.resumeFileName(savedAnalysis.getResumeFileName())
			.atsScore(savedAnalysis.getAtsScore())
			.overallFeedback(savedAnalysis.getOverallFeedback())
			.aiSuggestions(savedAnalysis.getAiSuggestions())
			.matchedKeywords(atsAnalysisResult.matchedKeywords())
			.missingKeywords(atsAnalysisResult.missingKeywords())
			.checklist(resumeSectionChecklistService.buildChecklist(savedAnalysis, atsAnalysisResult.missingKeywords()))
			.extractedTextPreview(createPreview(savedAnalysis.getResumeText()))
			.build();
	}

	@Transactional(readOnly = true)
	public List<AnalysisHistoryResponse> getAnalysisHistory(String userEmail) {
		User user = getUserByEmail(userEmail);

		return resumeAnalysisRepository.findByUserOrderByCreatedAtDesc(user)
			.stream()
			.map(this::toHistoryResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public AnalysisReportResponse getAnalysisReport(Long analysisId, String userEmail) {
		ResumeAnalysis analysis = getAnalysisForUser(analysisId, userEmail);

		return toReportResponse(analysis);
	}

	@Transactional
	public InterviewQuestionsResponse generateInterviewQuestions(Long analysisId, String userEmail) {
		ResumeAnalysis analysis = getAnalysisForUser(analysisId, userEmail);
		String interviewQuestions = interviewQuestionService.generateInterviewQuestions(
			analysis.getResumeText(),
			analysis.getJobDescription(),
			analysis.getAtsScore(),
			toMatchedKeywordList(analysis),
			toMissingKeywordList(analysis)
		);

		analysis.setInterviewQuestions(interviewQuestions);
		ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);

		return InterviewQuestionsResponse.builder()
			.analysisId(savedAnalysis.getId())
			.interviewQuestions(savedAnalysis.getInterviewQuestions())
			.build();
	}

	@Transactional
	public OptimizeResumeResponse optimizeResume(Long analysisId, String userEmail) {
		LOGGER.info("Starting resume optimization for analysisId={}", analysisId);
		ResumeAnalysis analysis = getAnalysisForUser(analysisId, userEmail);
		validateAnalysisForOptimization(analysis);
		List<String> missingKeywords = toMissingKeywordList(analysis);
		LOGGER.info(
			"Loaded analysisId={} for optimization. missingKeywordsCount={}",
			analysis.getId(),
			missingKeywords.size()
		);
		String optimizedResumeText = resumeOptimizationService.optimizeResume(
			analysis.getResumeText(),
			analysis.getJobDescription(),
			analysis.getAtsScore(),
			toMatchedKeywordList(analysis),
			missingKeywords,
			analysis.getAiSuggestions()
		);
		boolean optimizationUnavailable = isOptimizationUnavailable(optimizedResumeText);
		if (optimizationUnavailable) {
			LOGGER.warn("Resume optimization returned fallback for analysisId={}", analysis.getId());
		} else {
			LOGGER.info("Resume optimization generated content for analysisId={}", analysis.getId());
		}
		List<String> missingKeywordsUsed = optimizationUnavailable
			? List.of()
			: findKeywordsUsed(optimizedResumeText, missingKeywords);

		if (!optimizationUnavailable) {
			analysis.setOptimizedResumeText(optimizedResumeText);
			resumeAnalysisRepository.save(analysis);
		}

		return OptimizeResumeResponse.builder()
			.analysisId(analysis.getId())
			.originalAtsScore(analysis.getAtsScore())
			.optimizedResumeText(optimizationUnavailable ? "" : analysis.getOptimizedResumeText())
			.missingKeywordsUsed(missingKeywordsUsed)
			.message(optimizationUnavailable
				? optimizedResumeText
				: "Optimized resume generated successfully. Review before using.")
			.build();
	}

	@Transactional(readOnly = true)
	public byte[] downloadPdfReport(Long analysisId, String userEmail) {
		ResumeAnalysis analysis = getAnalysisForUser(analysisId, userEmail);

		return pdfReportService.generateReport(analysis);
	}

	@Transactional(readOnly = true)
	public byte[] downloadOptimizedResumePdf(Long analysisId, String userEmail) {
		ResumeAnalysis analysis = getAnalysisForUser(analysisId, userEmail);
		validateOptimizedResumeExists(analysis);

		return optimizedResumeDocumentService.generatePdf(analysis);
	}

	@Transactional(readOnly = true)
	public byte[] downloadOptimizedResumeDocx(Long analysisId, String userEmail) {
		ResumeAnalysis analysis = getAnalysisForUser(analysisId, userEmail);
		validateOptimizedResumeExists(analysis);

		return optimizedResumeDocumentService.generateDocx(analysis);
	}

	@Transactional(readOnly = true)
	public ResumeComparisonResponse compareAnalyses(Long oldAnalysisId, Long newAnalysisId, String userEmail) {
		validateComparisonIds(oldAnalysisId, newAnalysisId);

		User user = getUserByEmail(userEmail);
		ResumeAnalysis oldAnalysis = getAnalysisForComparison(oldAnalysisId, user);
		ResumeAnalysis newAnalysis = getAnalysisForComparison(newAnalysisId, user);

		Set<String> oldKeywords = toKeywordSet(oldAnalysis);
		Set<String> newKeywords = toKeywordSet(newAnalysis);

		List<String> newlyAddedKeywords = newKeywords.stream()
			.filter(keyword -> !oldKeywords.contains(keyword))
			.toList();
		List<String> removedKeywords = oldKeywords.stream()
			.filter(keyword -> !newKeywords.contains(keyword))
			.toList();

		int previousScore = oldAnalysis.getAtsScore();
		int currentScore = newAnalysis.getAtsScore();
		int scoreDifference = currentScore - previousScore;
		double improvementPercentage = calculateImprovementPercentage(previousScore, currentScore);

		return ResumeComparisonResponse.builder()
			.previousScore(previousScore)
			.currentScore(currentScore)
			.scoreDifference(scoreDifference)
			.improvementPercentage(improvementPercentage)
			.newlyAddedKeywords(newlyAddedKeywords)
			.removedKeywords(removedKeywords)
			.recommendation(createComparisonRecommendation(scoreDifference, newlyAddedKeywords, removedKeywords))
			.build();
	}

	private void validateComparisonIds(Long oldAnalysisId, Long newAnalysisId) {
		if (oldAnalysisId == null || newAnalysisId == null || oldAnalysisId <= 0 || newAnalysisId <= 0) {
			throw new AnalysisNotFoundException("Invalid analysis id");
		}

		if (oldAnalysisId.equals(newAnalysisId)) {
			throw new FileUploadException("Please select two different resume analyses to compare");
		}
	}

	private ResumeAnalysis getAnalysisForComparison(Long analysisId, User user) {
		if (user.getRole() == Role.ADMIN) {
			return resumeAnalysisRepository.findById(analysisId)
				.orElseThrow(() -> new AnalysisNotFoundException("Analysis not found"));
		}

		return resumeAnalysisRepository.findByIdAndUser(analysisId, user)
			.orElseThrow(() -> resolveReportAccessException(analysisId));
	}

	private Set<String> toKeywordSet(ResumeAnalysis analysis) {
		Set<String> keywords = new LinkedHashSet<>();
		for (MatchedKeyword matchedKeyword : analysis.getMatchedKeywords()) {
			keywords.add(matchedKeyword.getKeyword());
		}

		return keywords;
	}

	private double calculateImprovementPercentage(int previousScore, int currentScore) {
		if (previousScore == 0) {
			return currentScore > 0 ? 100.0 : 0.0;
		}

		return BigDecimal.valueOf(currentScore - previousScore)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(previousScore), 2, RoundingMode.HALF_UP)
			.doubleValue();
	}

	private String createComparisonRecommendation(
		int scoreDifference,
		List<String> newlyAddedKeywords,
		List<String> removedKeywords
	) {
		if (scoreDifference > 0) {
			return "Great progress. Keep the newly matched keywords and continue strengthening missing role-specific skills.";
		}

		if (scoreDifference == 0 && !newlyAddedKeywords.isEmpty()) {
			return "Keyword coverage changed, but the score stayed stable. Keep the stronger additions and review removed keywords before finalizing.";
		}

		if (scoreDifference == 0) {
			return "Your score is unchanged. Add more relevant job-description keywords and improve project or experience bullets.";
		}

		return "The newer version scored lower. Re-add important removed keywords and align the resume more closely with the job description.";
	}

	private RuntimeException resolveReportAccessException(Long analysisId) {
		if (resumeAnalysisRepository.existsById(analysisId)) {
			return new UnauthorizedAccessException("You are not allowed to access this analysis");
		}

		return new AnalysisNotFoundException("Analysis not found");
	}

	private User getUserByEmail(String userEmail) {
		return userRepository.findByEmail(userEmail)
			.orElseThrow(() -> new UserNotFoundException("User not found"));
	}

	private ResumeAnalysis getAnalysisForUser(Long analysisId, String userEmail) {
		if (analysisId == null || analysisId <= 0) {
			throw new AnalysisNotFoundException("Invalid analysis id");
		}

		User user = getUserByEmail(userEmail);
		return user.getRole() == Role.ADMIN
			? resumeAnalysisRepository.findById(analysisId)
				.orElseThrow(() -> new AnalysisNotFoundException("Analysis not found"))
			: resumeAnalysisRepository.findByIdAndUser(analysisId, user)
				.orElseThrow(() -> resolveReportAccessException(analysisId));
	}

	private void validateAnalysisForOptimization(ResumeAnalysis analysis) {
		if (analysis.getResumeText() == null || analysis.getResumeText().isBlank()) {
			throw new FileUploadException("Resume text is missing for this analysis.");
		}

		if (analysis.getJobDescription() == null || analysis.getJobDescription().isBlank()) {
			throw new FileUploadException("Job description is missing for this analysis.");
		}
	}

	private void validateOptimizedResumeExists(ResumeAnalysis analysis) {
		if (analysis.getOptimizedResumeText() == null
			|| analysis.getOptimizedResumeText().isBlank()
			|| isOptimizationUnavailable(analysis.getOptimizedResumeText().trim())) {
			throw new FileUploadException("Please optimize resume first.");
		}
	}

	private boolean isOptimizationUnavailable(String optimizedResumeText) {
		return ResumeOptimizationService.FALLBACK_MESSAGE.equals(optimizedResumeText)
			|| GeminiService.MISSING_API_KEY_MESSAGE.equals(optimizedResumeText)
			|| GeminiService.EMPTY_RESPONSE_MESSAGE.equals(optimizedResumeText)
			|| GeminiService.MODEL_UNAVAILABLE_MESSAGE.equals(optimizedResumeText)
			|| GeminiService.INVALID_API_KEY_MESSAGE.equals(optimizedResumeText)
			|| GeminiService.RATE_LIMIT_MESSAGE.equals(optimizedResumeText);
	}

	private List<String> findKeywordsUsed(String optimizedResumeText, List<String> missingKeywords) {
		String normalizedResume = optimizedResumeText == null ? "" : optimizedResumeText.toLowerCase();

		return missingKeywords.stream()
			.filter(keyword -> normalizedResume.contains(keyword.toLowerCase()))
			.toList();
	}

	private void addMatchedKeywords(ResumeAnalysis analysis, Iterable<String> keywords) {
		for (String keyword : keywords) {
			MatchedKeyword matchedKeyword = MatchedKeyword.builder()
				.keyword(keyword)
				.resumeAnalysis(analysis)
				.build();
			analysis.getMatchedKeywords().add(matchedKeyword);
		}
	}

	private void addMissingKeywords(ResumeAnalysis analysis, Iterable<String> keywords) {
		for (String keyword : keywords) {
			MissingKeyword missingKeyword = MissingKeyword.builder()
				.keyword(keyword)
				.resumeAnalysis(analysis)
				.build();
			analysis.getMissingKeywords().add(missingKeyword);
		}
	}

	private void validateUpload(MultipartFile file, String jobDescription) {
		if (file == null || file.isEmpty()) {
			throw new FileUploadException("PDF resume file is required");
		}

		if (file.getSize() > maxFileSizeBytes) {
			throw new FileUploadException("PDF resume file size exceeds the allowed limit");
		}

		if (!isPdf(file)) {
			throw new FileUploadException("Only PDF files are allowed");
		}

		if (jobDescription == null || jobDescription.isBlank()) {
			throw new FileUploadException("Job description cannot be empty");
		}
	}

	private boolean isPdf(MultipartFile file) {
		String contentType = file.getContentType();
		String fileName = file.getOriginalFilename();

		return PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)
			|| (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
	}

	private String createPreview(String resumeText) {
		if (resumeText.length() <= PREVIEW_LENGTH) {
			return resumeText;
		}

		return resumeText.substring(0, PREVIEW_LENGTH);
	}

	private AnalysisHistoryResponse toHistoryResponse(ResumeAnalysis analysis) {
		return AnalysisHistoryResponse.builder()
			.analysisId(analysis.getId())
			.resumeFileName(analysis.getResumeFileName())
			.atsScore(analysis.getAtsScore())
			.overallFeedback(analysis.getOverallFeedback())
			.createdAt(analysis.getCreatedAt())
			.build();
	}

	private AnalysisReportResponse toReportResponse(ResumeAnalysis analysis) {
		return AnalysisReportResponse.builder()
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
			.interviewQuestions(analysis.getInterviewQuestions())
			.optimizedResumeText(analysis.getOptimizedResumeText())
			.matchedKeywords(toMatchedKeywordList(analysis))
			.missingKeywords(toMissingKeywordList(analysis))
			.checklist(resumeSectionChecklistService.buildChecklist(analysis, toMissingKeywordList(analysis)))
			.createdAt(analysis.getCreatedAt())
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
