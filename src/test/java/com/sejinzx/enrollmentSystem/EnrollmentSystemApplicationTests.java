package com.sejinzx.enrollmentSystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EnrollmentSystemApplicationTests extends MySqlContainerTest {

	@Test
	void contextLoads() {
	}
}