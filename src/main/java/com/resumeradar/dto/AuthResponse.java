package com.resumeradar.dto;

import com.resumeradar.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {

	private String token;

	private String fullName;

	private String email;

	private Role role;
}
