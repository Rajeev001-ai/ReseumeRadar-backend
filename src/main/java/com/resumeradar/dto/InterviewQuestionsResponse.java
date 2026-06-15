package com.resumeradar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InterviewQuestionsResponse {

	private Long analysisId;

	private String interviewQuestions;
}
