package com.resumeradar.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OptimizeResumeResponse {

	private Long analysisId;

	private Integer originalAtsScore;

	private String optimizedResumeText;

	private List<String> missingKeywordsUsed;

	private String message;
}
