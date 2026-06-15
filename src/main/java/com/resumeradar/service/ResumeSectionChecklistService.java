package com.resumeradar.service;

import com.resumeradar.dto.ResumeChecklistItemResponse;
import com.resumeradar.entity.ResumeAnalysis;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ResumeSectionChecklistService {

	private static final Pattern SUMMARY_PATTERN = headingPattern(
		"professional summary|summary|career objective|objective|profile"
	);

	private static final Pattern SKILLS_PATTERN = headingPattern(
		"skills|technical skills|key skills|core competencies|technologies"
	);

	private static final Pattern PROJECTS_PATTERN = headingPattern(
		"projects|academic projects|personal projects|project experience"
	);

	private static final Pattern EXPERIENCE_PATTERN = headingPattern(
		"experience|work experience|professional experience|employment|internship|internships"
	);

	private static final Pattern EDUCATION_PATTERN = headingPattern(
		"education|academic background|educational qualification|qualifications"
	);

	private static final Pattern CERTIFICATIONS_PATTERN = headingPattern(
		"certifications|certificates|licenses|courses"
	);

	private static final Pattern ACHIEVEMENTS_PATTERN = headingPattern(
		"achievements|awards|honors|accomplishments"
	);

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
		"(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b"
	);

	private static final Pattern PHONE_PATTERN = Pattern.compile(
		"(?i)(?:\\+?\\d{1,3}[\\s.-]?)?(?:\\(?\\d{3,5}\\)?[\\s.-]?)?\\d{3,5}[\\s.-]?\\d{4}\\b"
	);

	private static final Pattern CONTACT_LINK_PATTERN = Pattern.compile(
		"(?i)\\b(linkedin|github|portfolio|mailto:)\\b"
	);

	public void detectSections(ResumeAnalysis analysis) {
		String resumeText = analysis.getResumeText();

		analysis.setHasSummary(matches(SUMMARY_PATTERN, resumeText));
		analysis.setHasSkills(matches(SKILLS_PATTERN, resumeText));
		analysis.setHasProjects(matches(PROJECTS_PATTERN, resumeText));
		analysis.setHasExperience(matches(EXPERIENCE_PATTERN, resumeText));
		analysis.setHasEducation(matches(EDUCATION_PATTERN, resumeText));
		analysis.setHasCertifications(matches(CERTIFICATIONS_PATTERN, resumeText));
		analysis.setHasAchievements(matches(ACHIEVEMENTS_PATTERN, resumeText));
		analysis.setHasContactInfo(hasContactInfo(resumeText));
	}

	public List<ResumeChecklistItemResponse> buildChecklist(
		ResumeAnalysis analysis,
		List<String> missingKeywords
	) {
		return List.of(
			item("Summary", analysis.isHasSummary(), shouldImproveSummary(analysis), "Summary section is present. Keep it focused on the target role.", "Add a short professional summary or objective aligned to the job description.", "Improve the summary with role-specific wording and measurable strengths."),
			item("Skills", analysis.isHasSkills(), missingKeywords != null && !missingKeywords.isEmpty(), "Skills section is present. Add missing JD keywords if relevant.", "Add a dedicated skills section with tools, technologies, and role-specific keywords.", "Skills section is present. Add missing JD keywords naturally if you have those skills."),
			item("Projects", analysis.isHasProjects(), shouldImproveCoreSection(analysis), "Projects section is present. Keep project bullets impact-oriented.", "Add relevant projects with tech stack, problem solved, and measurable outcomes.", "Improve project bullets with technologies, action verbs, and outcomes."),
			item("Experience", analysis.isHasExperience(), shouldImproveCoreSection(analysis), "Experience or internship section is present.", "Add experience, internship, freelance, or practical work details if available.", "Improve experience bullets with results, metrics, and job-aligned responsibilities."),
			item("Education", analysis.isHasEducation(), false, "Education section is present.", "Add education details such as degree, institution, and year.", ""),
			item("Certifications", analysis.isHasCertifications(), false, "Certifications section is present.", "Add relevant certifications or courses if you have them.", ""),
			item("Achievements", analysis.isHasAchievements(), false, "Achievements section is present.", "Add awards, achievements, hackathons, publications, or measurable wins if applicable.", ""),
			item("Contact Info", analysis.isHasContactInfo(), false, "Contact information is present.", "Add email, phone, LinkedIn, GitHub, or portfolio links near the top.", "")
		);
	}

	private ResumeChecklistItemResponse item(
		String sectionName,
		boolean present,
		boolean improve,
		String goodSuggestion,
		String missingSuggestion,
		String improveSuggestion
	) {
		String status = present ? (improve ? "IMPROVE" : "GOOD") : "MISSING";
		String suggestion = present ? (improve ? improveSuggestion : goodSuggestion) : missingSuggestion;

		return ResumeChecklistItemResponse.builder()
			.sectionName(sectionName)
			.present(present)
			.status(status)
			.suggestion(suggestion)
			.build();
	}

	private boolean shouldImproveSummary(ResumeAnalysis analysis) {
		return score(analysis) < 60;
	}

	private boolean shouldImproveCoreSection(ResumeAnalysis analysis) {
		return score(analysis) < 70;
	}

	private int score(ResumeAnalysis analysis) {
		return analysis.getAtsScore() == null ? 0 : analysis.getAtsScore();
	}

	private boolean hasContactInfo(String resumeText) {
		return matches(EMAIL_PATTERN, resumeText)
			|| matches(PHONE_PATTERN, resumeText)
			|| matches(CONTACT_LINK_PATTERN, resumeText);
	}

	private boolean matches(Pattern pattern, String value) {
		return value != null && pattern.matcher(value).find();
	}

	private static Pattern headingPattern(String alternatives) {
		return Pattern.compile("(?im)^\\s*(?:#+\\s*)?(?:" + alternatives + ")\\s*:?.*$");
	}
}
