package com.alphago365.octopus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private String secret;
    private long expiration;
}
