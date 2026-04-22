package com.garygz.dspdemoproject.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

@Configuration
public class JwtSecretConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecretConfig.class);

    private final String secretName;
    private final String fallbackSecret;

    public JwtSecretConfig(
            @Value("${auth.jwt.secret-name:}") String secretName,
            @Value("${auth.jwt.secret:change-me-in-production-must-be-32-chars}") String fallbackSecret) {
        this.secretName = secretName;
        this.fallbackSecret = fallbackSecret;
    }

    @Bean
    public String jwtSecret() {
        if (secretName.isBlank()) {
            logger.warn("auth.jwt.secret-name not set — using fallback JWT secret");
            return fallbackSecret;
        }
        try (SecretsManagerClient client = SecretsManagerClient.create()) {
            String secret = client.getSecretValue(
                    GetSecretValueRequest.builder().secretId(secretName).build()
            ).secretString();
            logger.info("JWT secret loaded from AWS Secrets Manager: {}", secretName);
            return secret;
        } catch (SecretsManagerException e) {
            logger.warn("Failed to fetch JWT secret '{}' from Secrets Manager — using fallback: {}", secretName, e.getMessage());
            return fallbackSecret;
        }
    }
}
