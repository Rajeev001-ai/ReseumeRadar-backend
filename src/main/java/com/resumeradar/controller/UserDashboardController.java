package com.resumeradar.controller;

import com.resumeradar.dto.UserDashboardAnalyticsResponse;
import com.resumeradar.service.UserDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/dashboard")
public class UserDashboardController {

	private final UserDashboardService userDashboardService;

	public UserDashboardController(UserDashboardService userDashboardService) {
		this.userDashboardService = userDashboardService;
	}

	@GetMapping("/analytics")
	public UserDashboardAnalyticsResponse getDashboardAnalytics(Authentication authentication) {
		return userDashboardService.getAnalytics(authentication.getName());
	}
}
