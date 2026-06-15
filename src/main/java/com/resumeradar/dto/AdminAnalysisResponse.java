package com.resumeradar.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AdminAnalysisResponse {

	private Long analysisId;

	private String resumeFileName;

	private Integer atsScore;

	private String overallFeedback;

	private LocalDateTime createdAt;

	private String userFullName;

	private String userEmail;
}
