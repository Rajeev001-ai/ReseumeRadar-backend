package com.resumeradar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AdminDashboardResponse {

	private Long totalUsers;

	private Long totalAnalyses;

	private Double averageAtsScore;

	private Long excellentReportsCount;

	private Long goodReportsCount;

	private Long averageReportsCount;

	private Long poorReportsCount;
}
