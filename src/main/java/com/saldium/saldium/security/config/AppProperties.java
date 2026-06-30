package com.saldium.saldium.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendUrl,
        String backendUrl,
        List<String> corsAllowedOrigins
) {
}
