package com.resumeradar.service;

import com.resumeradar.dto.AtsAnalysisResult;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class AtsAnalyzerService {

	private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^a-z0-9+#.\\s]");

	private static final Set<String> STOP_WORDS = Set.of(
		"a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "have",
		"in", "is", "it", "its", "of", "on", "or", "our", "that", "the", "their", "this",
		"to", "with", "you", "your", "we", "will", "can", "must", "should", "about", "into",
		"over", "under", "within", "without", "using", "use", "used", "such", "etc", "than",
		"then", "they", "them", "these", "those", "who", "what", "when", "where", "why", "how",
		"also", "more", "most", "other", "some", "any", "all", "each", "per", "via", "work",
		"working", "team", "teams", "candidate", "candidates", "role", "job", "position",
		"responsibilities", "requirements", "required", "preferred", "experience", "years"
	);

	public AtsAnalysisResult analyze(String resumeText, String jobDescription) {
		Set<String> jobKeywords = extractKeywords(jobDescription);
		Set<String> resumeKeywords = extractKeywords(resumeText);

		List<String> matchedKeywords = new ArrayList<>();
		List<String> missingKeywords = new ArrayList<>();

		for (String keyword : jobKeywords) {
			if (resumeKeywords.contains(keyword)) {
				matchedKeywords.add(keyword);
			} else {
				missingKeywords.add(keyword);
			}
		}

		int atsScore = calculateScore(matchedKeywords.size(), jobKeywords.size());
		String feedback = createFeedback(atsScore);

		return new AtsAnalysisResult(atsScore, feedback, matchedKeywords, missingKeywords);
	}

	private Set<String> extractKeywords(String text) {
		Set<String> keywords = new LinkedHashSet<>();

		if (text == null || text.isBlank()) {
			return keywords;
		}

		String normalizedText = NON_WORD_PATTERN.matcher(text.toLowerCase()).replaceAll(" ");
		String[] words = normalizedText.split("\\s+");

		for (String word : words) {
			String keyword = word.trim();

			if (isMeaningfulKeyword(keyword)) {
				keywords.add(keyword);
			}
		}

		return keywords;
	}

	private boolean isMeaningfulKeyword(String keyword) {
		return keyword.length() >= 3 && !STOP_WORDS.contains(keyword);
	}

	private int calculateScore(int matchedCount, int totalKeywords) {
		if (totalKeywords == 0) {
			return 0;
		}

		return (int) Math.round((matchedCount * 100.0) / totalKeywords);
	}

	private String createFeedback(int atsScore) {
		if (atsScore >= 80) {
			return "Excellent match";
		}

		if (atsScore >= 60) {
			return "Good match but can improve";
		}

		if (atsScore >= 40) {
			return "Average match, needs improvement";
		}

		return "Poor match, resume needs major improvement";
	}
}
