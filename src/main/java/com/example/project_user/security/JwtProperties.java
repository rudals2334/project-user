package com.example.project_user.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessExpMinutes = 30;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessExpMinutes() {
        return accessExpMinutes;
    }

    public void setAccessExpMinutes(long accessExpMinutes) {
        this.accessExpMinutes = accessExpMinutes;
    }
}


