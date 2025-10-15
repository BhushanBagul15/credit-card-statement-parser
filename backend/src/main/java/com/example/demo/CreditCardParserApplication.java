package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class CreditCardParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditCardParserApplication.class, args);
		System.out.println("\n" + "=".repeat(60));
		System.out.println("🚀 Credit Card Statement Parser API is RUNNING!");
		System.out.println("=".repeat(60));
		System.out.println("📍 Server URL:        http://localhost:8080");
		System.out.println("📚 API Documentation: http://localhost:8080/swagger-ui.html");
		System.out.println("❤️  Health Check:     http://localhost:8080/api/statements/health");
		System.out.println("=".repeat(60) + "\n");
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
						.allowedOrigins(
								"http://localhost:3000",  // React default
								"http://localhost:5173",  // Vite default ← IMPORTANT!
								"http://localhost:4200"   // Angular (if needed)
						)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true)
						.maxAge(3600);
			}
		};
	}

}
