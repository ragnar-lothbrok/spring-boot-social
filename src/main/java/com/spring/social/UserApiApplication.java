package com.spring.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.spring.social.account.model.MyAccessToken;

@SpringBootApplication
@EnableTransactionManagement
@EnableAutoConfiguration
public class UserApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApiApplication.class, args);
	}

	@Bean
	MyAccessToken myAccessToken() {
		return new MyAccessToken();
	}
}
