package com.thiagsilvadev.erpapi.iam.internal.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp.iam.jwt")
public class IamJwtProperties implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalStateException("JWT secret must contain at least 32 bytes and not be blank");
        if (issuer == null || issuer.isBlank()) throw new IllegalStateException("JWT issuer must not be blank");
        if (expiration == null || expiration.getSeconds() < 1 || expiration.getNano() != 0)
            throw new IllegalStateException("JWT expiration must be positive whole seconds");
    }

    private String secret;

    private String issuer = "erp-api";

    private Duration expiration = Duration.ofHours(1);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }
}
