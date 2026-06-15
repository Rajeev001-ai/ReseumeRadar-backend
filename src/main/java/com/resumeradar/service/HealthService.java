package com.resumeradar.service;

import org.springframework.stereotype.Service;

@Service
public class HealthService {

	public String getStatus() {
		return "ResumeRadar API is running";
	}
}
