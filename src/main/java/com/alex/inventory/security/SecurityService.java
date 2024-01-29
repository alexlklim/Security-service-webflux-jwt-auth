package com.alex.inventory.security;


import com.alex.inventory.entity.UserEntity;
import com.alex.inventory.entity.enums.ErrorCode;
import com.alex.inventory.exceptions.AuthException;
import com.alex.inventory.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SecurityService {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expirationInSeconds;
    @Value("${jwt.issuer}")
    private String issuer;

    private TokenDetails generateToken(UserEntity userEntity){

        Map<String, Object> claims = new HashMap<>(){{
            put("role", userEntity.getRole());
            put("username", userEntity.getUsername());
        }};

        return generateToken(claims, userEntity.getId().toString());
    }

    private TokenDetails generateToken(Map<String, Object> claims, String subject){
        long expirationTimeInMillis = expirationInSeconds * 1000L;
        Date expirationDate = new Date(new Date().getTime() + expirationTimeInMillis);

        return generateToken(expirationDate, claims, subject );
    }

    private TokenDetails generateToken(Date expirationDate, Map<String, Object> claims, String subject){
        Date createdDate = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setId(UUID.randomUUID().toString())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();


        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }


    private final UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public Mono<TokenDetails> authenticate(String username, String password) {
        return userService.getUserByUserName(username)
                .flatMap(user -> {
                    if (!user.isEnabled())
                        return Mono.error(new AuthException("Account disabled", ErrorCode.USER_ACCOUNT_DISABLED.name()));

                    if (!passwordEncoder.matches(password, user.getPassword()))
                        return Mono.error(new AuthException("Invalid Password", ErrorCode.INVALID_PASSWORD.name()));

                    return Mono.just(generateToken(user).toBuilder()
                            .userId(user.getId())
                            .build()
                    );
                })
                .switchIfEmpty(Mono.error(
                        new AuthException("Invalid username", ErrorCode.INVALID_USERNAME.name())
                ));
    }
}
