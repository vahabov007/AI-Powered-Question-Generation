package com.vahabvahabov.AI_Powered_Question_Generation_Module.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HuggingFaceConfig {

    @Value("${huggingface.api.key}")
    private String apiKey;
    @Value("${huggingface.api.url}")
    private String baseUrl;

    @Bean
    public WebClient huggingFaceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
}