package com.vahabvahabov.AI_Powered_Question_Generation_Module;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AiPoweredQuestionGenerationModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiPoweredQuestionGenerationModuleApplication.class, args);
	}

}
