package com.resumeradar.config;

import com.resumeradar.entity.Role;
import com.resumeradar.entity.User;
import com.resumeradar.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserSeeder {

	private static final String ADMIN_EMAIL = "admin@resumeradar.com";
	private static final String ADMIN_PASSWORD = "admin123";

	@Bean
	public CommandLineRunner seedAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.existsByEmail(ADMIN_EMAIL)) {
				return;
			}

			User admin = User.builder()
				.fullName("ResumeRadar Admin")
				.email(ADMIN_EMAIL)
				.password(passwordEncoder.encode(ADMIN_PASSWORD))
				.role(Role.ADMIN)
				.build();

			userRepository.save(admin);
		};
	}
}
