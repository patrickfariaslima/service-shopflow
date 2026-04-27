package com.shopflow.shopflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ShopflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopflowApplication.class, args);
	}

}
