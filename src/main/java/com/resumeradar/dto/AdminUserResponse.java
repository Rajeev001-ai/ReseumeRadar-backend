package com.resumeradar.dto;

import com.resumeradar.entity.Role;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AdminUserResponse {

	private Long userId;

	private String fullName;

	private String email;

	private Role role;

	private LocalDateTime createdAt;

	private Long totalAnalyses;
}
