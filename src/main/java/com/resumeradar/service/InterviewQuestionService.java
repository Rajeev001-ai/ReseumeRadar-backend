package com.resumeradar.service;

import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InterviewQuestionService {

	public static final String FALLBACK_MESSAGE =
		"Interview questions are currently unavailable. Please try again later.";

	private final GeminiService geminiService;

	public InterviewQuestionService(GeminiService geminiService) {
		this.geminiService = geminiService;
	}

	public String generateInterviewQuestions(
		String resumeText,
		String jobDescription,
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords
	) {
		return geminiService.generateContent(
			createPrompt(resumeText, jobDescription, atsScore, matchedKeywords, missingKeywords),
			FALLBACK_MESSAGE,
			"Interview questions",
			Duration.ofSeconds(30)
		);
	}

	private String createPrompt(
		String resumeText,
		String jobDescription,
		Integer atsScore,
		List<String> matchedKeywords,
		List<String> missingKeywords
	) {
		return """
			You are a senior technical interviewer.

			Generate tailored interview questions for this candidate and job description.
			Use the candidate's resume, job description, ATS score, matched keywords, and missing keywords.
			Do not invent candidate experience. Questions should help an interviewer verify real skills and project depth.

			Use these section headings exactly:
			1. Technical Questions
			2. Java/Spring Boot Questions
			3. Project-Based Questions
			4. Resume-Based Questions
			5. HR Questions

			Write 5 concise questions under each section. Keep the format readable with numbered questions.

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
