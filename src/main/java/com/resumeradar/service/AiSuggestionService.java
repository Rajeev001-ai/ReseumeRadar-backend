package com.resumeradar.service;

import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiSuggestionService {

	public static final String FALLBACK_MESSAGE =
		"AI suggestions are currently unavailable. Please try again later.";

	private final GeminiService geminiService;

	public AiSuggestionService(GeminiService geminiService) {
		this.geminiService = geminiService;
	}

	public String generateSuggestions(
		String resumeText,
		String jobDescription,
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords
	) {
		String suggestions = geminiService.generateContent(
			createPrompt(resumeText, jobDescription, atsScore, matchedKeywords, missingKeywords),
			FALLBACK_MESSAGE,
			"AI suggestions",
			Duration.ofSeconds(30)
		);
		if (isFallbackMessage(suggestions)
			|| GeminiService.MISSING_API_KEY_MESSAGE.equals(suggestions)
			|| GeminiService.EMPTY_RESPONSE_MESSAGE.equals(suggestions)
			|| GeminiService.MODEL_UNAVAILABLE_MESSAGE.equals(suggestions)
			|| GeminiService.INVALID_API_KEY_MESSAGE.equals(suggestions)
			|| GeminiService.RATE_LIMIT_MESSAGE.equals(suggestions)) {
			return generateLocalSuggestions(atsScore, matchedKeywords, missingKeywords);
		}

		return suggestions;
	}

	public String resolveSuggestions(
		String currentSuggestions,
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords
	) {
		if (currentSuggestions == null || currentSuggestions.isBlank() || isFallbackMessage(currentSuggestions)) {
			return generateLocalSuggestions(atsScore, matchedKeywords, missingKeywords);
		}

		return currentSuggestions;
	}

	public boolean isFallbackMessage(String suggestions) {
		return FALLBACK_MESSAGE.equalsIgnoreCase(suggestions == null ? "" : suggestions.trim());
	}

	public String generateLocalSuggestions(
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords
	) {
		int score = atsScore == null ? 0 : atsScore;
		List<String> topMissingKeywords = safeList(missingKeywords).stream()
			.limit(12)
			.toList();
		List<String> topMatchedKeywords = safeList(matchedKeywords).stream()
			.limit(8)
			.toList();
		String missingKeywordText = topMissingKeywords.isEmpty()
			? "No major missing keywords were detected. Focus on clarity, metrics, and role alignment."
			: String.join(", ", topMissingKeywords);
		String matchedKeywordText = topMatchedKeywords.isEmpty()
			? "No strong keyword matches were detected yet."
			: String.join(", ", topMatchedKeywords);

		return """
			1. Professional summary improvement
			- Start with a 2-3 line summary that clearly matches the target role.
			- Mention your strongest matched skills: %s.
			- Add one measurable achievement if possible, such as project impact, accuracy, users, time saved, or performance improvement.

			2. Skills section improvement
			- Group skills into clear categories such as Programming, Frameworks, Tools, Databases, and Soft Skills.
			- Add the most relevant missing keywords only if you truly have experience with them: %s.

			3. Missing keywords to add
			- Review these keywords from the job description and add them naturally where they fit: %s.
			- Avoid keyword stuffing. Place skills in summary, skills, projects, or experience bullets where they are truthful.

			4. Project description improvement
			- Rewrite project bullets using action verbs, technical stack, problem solved, and measurable result.
			- Example structure: Built/Designed/Implemented [feature] using [technology] to achieve [result].

			5. Experience/internship bullet improvement
			- Convert responsibility-based bullets into impact-based bullets.
			- Use numbers wherever possible: percentages, counts, users, response time, accuracy, automation time, or project scope.

			6. Formatting tips
			- Keep the resume ATS-friendly with simple headings, consistent spacing, and no complex tables or graphics.
			- Put the most job-relevant skills and projects near the top.
			- Current ATS score is %d%%, so prioritize keyword alignment and measurable impact before applying.
			""".formatted(
			matchedKeywordText,
			missingKeywordText,
			missingKeywordText,
			score
		).trim();
	}

	private String createPrompt(
		String resumeText,
		String jobDescription,
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords
	) {
		return """
			You are an ATS resume optimization assistant.

			Review the resume against the job description and provide clear, practical, bullet-point suggestions.
			Do not invent experience. Suggest wording and improvements only from the candidate's existing background.

			Include these sections exactly:
			1. Professional summary improvement
			2. Skills section improvement
			3. Missing keywords to add
			4. Project description improvement
			5. Experience/internship bullet improvement
			6. Formatting tips

			ATS score: %s
			Matched keywords: %s
			Missing keywords: %s

			Job description:
			%s

			Resume text:
			%s
			""".formatted(
			atsScore,
			String.join(", ", safeList(matchedKeywords)),
			String.join(", ", safeList(missingKeywords)),
			jobDescription,
			truncate(resumeText, 12000)
		);
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}

		return value.substring(0, maxLength);
	}

	private List<String> safeList(List<String> values) {
		return values == null ? List.of() : values;
	}
}
