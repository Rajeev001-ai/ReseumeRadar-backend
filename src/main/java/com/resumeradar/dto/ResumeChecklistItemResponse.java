package com.resumeradar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ResumeChecklistItemResponse {

	private String sectionName;

	private boolean present;

	private String status;

	private String suggestion;
}
