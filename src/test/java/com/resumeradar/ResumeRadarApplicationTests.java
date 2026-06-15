package com.resumeradar;

import com.resumeradar.repository.UserRepository;
import com.resumeradar.repository.ResumeAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
	"spring.jpa.hibernate.ddl-auto=none",
	"DB_URL=jdbc:postgresql://localhost:5432/resumeradar_test",
	"DB_USERNAME=test",
	"DB_PASSWORD=test",
	"JWT_SECRET=Q2hhbmdlVGhpc1Jlc3VtZVJhZGFyVGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2g=",
	"GEMINI_API_KEY=test",
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ResumeRadarApplicationTests {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private ResumeAnalysisRepository resumeAnalysisRepository;

	@Test
	void contextLoads() {
	}
}
