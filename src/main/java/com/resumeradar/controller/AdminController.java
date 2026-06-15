package com.resumeradar.controller;

import com.resumeradar.dto.AdminAnalysisDetailResponse;
import com.resumeradar.dto.AdminAnalysisResponse;
import com.resumeradar.dto.AdminDashboardResponse;
import com.resumeradar.dto.AdminUserResponse;
import com.resumeradar.service.AdminService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/dashboard")
	public AdminDashboardResponse getDashboard() {
		return adminService.getDashboard();
	}

	@GetMapping("/users")
	public List<AdminUserResponse> getUsers() {
		return adminService.getUsers();
	}

	@GetMapping("/analyses")
	public List<AdminAnalysisResponse> getAnalyses() {
		return adminService.getAnalyses();
	}

	@GetMapping("/analyses/{analysisId}")
	public AdminAnalysisDetailResponse getAnalysisDetail(@PathVariable Long analysisId) {
		return adminService.getAnalysisDetail(analysisId);
	}
}
