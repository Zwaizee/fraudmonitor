package com.exam.fraudmonitorapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@SpringBootApplication
public class FraudMonitorAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudMonitorAppApplication.class, args);
	}

    @Bean
    public JavaMailSender mailSender(){
        return new JavaMailSenderImpl();
    }
}
