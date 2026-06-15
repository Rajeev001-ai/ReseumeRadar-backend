package com.resumeradar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

	@NotBlank(message = "Full name is required")
	@Size(max = 100, message = "Full name must be 100 characters or fewer")
	private String fullName;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 150, message = "Email must be 150 characters or fewer")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
	private String password;
}
