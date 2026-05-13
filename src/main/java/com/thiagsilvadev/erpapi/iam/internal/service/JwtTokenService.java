package com.thiagsilvadev.erpapi.iam.internal.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.thiagsilvadev.erpapi.iam.internal.config.IamJwtProperties;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final IamJwtProperties properties;
    private final Clock clock;

    public JwtTokenService(JwtEncoder jwtEncoder, IamJwtProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedToken issue(IamCredential credential) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.getExpiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(credential.getUserId().toString())
                .id(UUID.randomUUID().toString())
                .claim("email", credential.getEmail())
                .claim("role", credential.getRole())
                .claim("scope", "ROLE_" + credential.getRole())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token =
                jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedToken(token, properties.getExpiration().toSeconds());
    }

    public record IssuedToken(String value, long expiresIn) {}
}
