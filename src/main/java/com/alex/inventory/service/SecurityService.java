package com.alex.inventory.service;


import com.alex.inventory.dto.RefreshTokenDto;
import com.alex.inventory.entity.enums.ErrorCode;
import com.alex.inventory.exceptions.AuthException;
import com.alex.inventory.security.TokenDetails;
import com.alex.inventory.security.providers.TokenGenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityService {

    private final UserService userService;
    private final TokenGenProvider tokenGenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Mono<TokenDetails> authenticate(String username, String password) {
        return userService.getUserByUserName(username)
                .flatMap(user -> {
                    // check if user account is enabled
                    if (!user.isEnabled()) {
                        log.error(ErrorCode.USER_ACCOUNT_DISABLED.name());
                        return Mono.defer(() -> Mono.error(new AuthException("Incorrect data", ErrorCode.USER_ACCOUNT_DISABLED.name())));
                    }
                    // check if password is correct
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        log.error(ErrorCode.INVALID_PASSWORD.name());
                        return Mono.defer(() -> Mono.error(new AuthException("Incorrect data", ErrorCode.INVALID_PASSWORD.name())));
                    }
                    return Mono.defer(() -> tokenGenProvider.generateToken(user).map(
                            tokenDetails -> tokenDetails.toBuilder()
                                    .userId(user.getId()).build()));
                })
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new AuthException("INCORRECT DATA", ErrorCode.INVALID_USERNAME.name())
                )))
                .onErrorResume(AuthException.class, authException -> {
                    log.error("AUTHENTICATION ERROR: " + authException.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<TokenDetails> authenticateWithoutPassword(String username) {
        return userService.getUserByUserName(username)
                .flatMap(user -> {
                    // check if user account is enabled
                    if (!user.isEnabled()) {
                        log.error(ErrorCode.USER_ACCOUNT_DISABLED.name());
                        return Mono.defer(() -> Mono.error(new AuthException("Incorrect data", ErrorCode.USER_ACCOUNT_DISABLED.name())));
                    }
                    return Mono.defer(() -> tokenGenProvider.generateToken(user).map(
                            tokenDetails -> tokenDetails.toBuilder()
                                    .userId(user.getId()).build()));
                })
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new AuthException("INCORRECT DATA", ErrorCode.INVALID_USERNAME.name())
                )))
                .onErrorResume(AuthException.class, authException -> {
                    log.error("AUTHENTICATION ERROR: " + authException.getMessage());
                    return Mono.empty();
                });
    }
    public Mono<TokenDetails> refreshToken(RefreshTokenDto refreshTokenDto) {
        return refreshTokenService.getRefreshToken(refreshTokenDto)
                .filter(validToken -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime expiresAt = validToken.getExpiresAt();
                    return expiresAt.isAfter(now);
                })
                .flatMap(validToken -> userService.getUserById(validToken.getUserId())
                        .flatMap(user -> authenticateWithoutPassword(user.getUsername()))
                )
                .switchIfEmpty(Mono.empty());
    }



}

