package com.shopflow.shopflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShopflowApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void main_ShouldStartWithoutException() {
		assertDoesNotThrow(() -> ShopflowApplication.main(new String[]{}));
	}

}
