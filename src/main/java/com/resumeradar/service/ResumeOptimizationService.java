package com.resumeradar.service;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResumeOptimizationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResumeOptimizationService.class);

	public static final String FALLBACK_MESSAGE = "Resume optimization is currently unavailable.";

	private final GeminiService geminiService;

	public ResumeOptimizationService(GeminiService geminiService) {
		this.geminiService = geminiService;
	}

	public String optimizeResume(
		String resumeText,
		String jobDescription,
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords,
		String aiSuggestions
	) {
		LOGGER.info("Calling Gemini resume optimizer");
		return geminiService.generateContent(
			createPrompt(resumeText, jobDescription, atsScore, matchedKeywords, missingKeywords, aiSuggestions),
			FALLBACK_MESSAGE,
			"Resume optimization",
			Duration.ofSeconds(45)
		);
	}

	private String createPrompt(
		String resumeText,
		String jobDescription,
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords,
		String aiSuggestions
	) {
		return """
			You are an expert ATS resume editor.

			Rewrite and optimize the candidate's resume for the job description while following these strict rules:
			1. Do not add fake experience.
			2. Do not invent companies, degrees, certifications, or projects.
			3. Only improve wording using existing resume content.
			4. Naturally include missing keywords where relevant.
			5. Improve summary, skills, projects, and experience sections.
			6. Keep resume ATS-friendly.
			7. Use clean headings and bullet points.
			8. Keep content truthful and professional.

			Return only the optimized resume text. Do not include explanations, notes, markdown fences, or claims that are not supported by the original resume.

			Current ATS score: %s
			Matched keywords: %s
			Missing keywords to include only where truthful: %s

			AI suggestions:
			%s

			Job description:
			%s

			Original resume text:
			%s
			""".formatted(
			atsScore,
			String.join(", ", safeList(matchedKeywords)),
			String.join(", ", safeList(missingKeywords)),
			aiSuggestions == null ? "" : aiSuggestions,
			jobDescription,
			truncate(resumeText, 14000)
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
