package com.alex.inventory.security;

import com.alex.inventory.entity.UserEntity;
import com.alex.inventory.exceptions.UnauthorizedException;
import com.alex.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class AuthManager implements ReactiveAuthenticationManager {

    private final UserService userService;
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        return userService.getUserById(customPrincipal.getId())
                .filter(UserEntity::isEnabled)
                .switchIfEmpty(Mono.error(new UnauthorizedException("user diasabled")))
                .map(user -> authentication)
                ;
    }
}
