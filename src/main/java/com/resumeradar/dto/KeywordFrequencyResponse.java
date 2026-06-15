package com.resumeradar.dto;

import lombok.Builder;

@Builder
public record KeywordFrequencyResponse(
	String keyword,
	long count
) {
}
