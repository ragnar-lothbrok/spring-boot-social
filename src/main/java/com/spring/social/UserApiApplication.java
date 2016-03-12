package com.spring.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAutoConfiguration
public class UserApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApiApplication.class, args);
    }
}
