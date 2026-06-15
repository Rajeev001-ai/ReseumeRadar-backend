package com.resumeradar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI resumeRadarOpenApi() {
		String bearerScheme = "bearerAuth";

		return new OpenAPI()
			.info(new Info()
				.title("ResumeRadar API")
				.version("1.0.0")
				.description("ATS resume analysis, authentication, dashboard analytics, and reporting APIs."))
			.addSecurityItem(new SecurityRequirement().addList(bearerScheme))
			.components(new Components().addSecuritySchemes(
				bearerScheme,
				new SecurityScheme()
					.name(bearerScheme)
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
			));
	}
}
