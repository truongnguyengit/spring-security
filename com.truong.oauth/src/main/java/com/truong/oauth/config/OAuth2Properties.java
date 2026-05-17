package com.truong.oauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2")
public record OAuth2Properties(
        AuthorizationServer authorizationServer,
        Token token
) {
    public record AuthorizationServer(
            String issuer,
            String rsaPrivateKey,
            String rsaPublicKey,
            String keyId
    ) {}

    public record Token(
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds,
            boolean reuseRefreshToken
    ) {}
}
