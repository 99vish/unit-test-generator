package com.blumeglobal.tests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class UnitTestGeneratorApplication {

	public static void main(String[] args) {

		SpringApplication.run(UnitTestGeneratorApplication.class, args);

	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("*") // Allow connections from all origins
						.allowedMethods("*") // Allow all HTTP methods
						.allowedHeaders("*"); // Allow all headers
			}
		};
	}


}
