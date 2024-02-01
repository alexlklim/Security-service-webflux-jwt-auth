package com.alex.inventory.security.providers;

import com.alex.inventory.entity.UserEntity;
import com.alex.inventory.security.TokenDetails;
import com.alex.inventory.service.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;



@Component
@RequiredArgsConstructor
public class TokenGenProvider {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${jwt.issuer}")
    private String issuer;

    private final RefreshTokenService refreshTokenService;

    public Mono<TokenDetails> generateToken(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>(){{
            put("role", userEntity.getRole());
            put("username", userEntity.getUsername());
        }};

        long expirationTimeInMillis = expirationInSeconds * 1000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeInMillis);
        String subject = userEntity.getId().toString();

        Date createdDate = new Date();

        // Generate the JWT token
        String token = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();

        // Generate the refresh token and use it
        return refreshTokenService.generateRefreshToken(userEntity)
                .map(refreshTokenObj -> {
                    return TokenDetails.builder()
                            .token(token)
                            .issuedAt(createdDate)
                            .expiresAt(expirationDate)
                            .refreshToken(refreshTokenObj.getRefreshToken())
                            .role(userEntity.getRole().name())
                            .build();
                });
    }
}
