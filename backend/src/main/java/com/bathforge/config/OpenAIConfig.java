package com.bathforge.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for OpenAI service integration.
 */
@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.timeout:30}")
    private int timeoutSeconds;

    /**
     * Creates and configures the OpenAI service bean.
     *
     * @return configured OpenAiService instance
     */
    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofSeconds(timeoutSeconds));
    }
}