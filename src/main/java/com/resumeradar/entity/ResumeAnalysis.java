package com.resumeradar.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String resumeFileName;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String resumeText;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String jobDescription;

	@Column(nullable = false)
	private Integer atsScore;

	private boolean hasSummary;

	private boolean hasSkills;

	private boolean hasProjects;

	private boolean hasExperience;

	private boolean hasEducation;

	private boolean hasCertifications;

	private boolean hasAchievements;

	private boolean hasContactInfo;

	@Column(columnDefinition = "TEXT")
	private String overallFeedback;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String aiSuggestions;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String interviewQuestions;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String optimizedResumeText;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder.Default
	@OneToMany(mappedBy = "resumeAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<MatchedKeyword> matchedKeywords = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "resumeAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<MissingKeyword> missingKeywords = new ArrayList<>();

	@jakarta.persistence.PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
