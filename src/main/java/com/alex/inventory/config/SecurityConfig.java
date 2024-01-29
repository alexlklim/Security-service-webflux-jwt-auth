package com.alex.inventory.config;


import com.alex.inventory.security.AuthManager;
import com.alex.inventory.security.BearerTokenServerAuthConverter;
import com.alex.inventory.security.TokenAuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Value("${jwt.secret}")
    private String secret;


    private final String[] publicRoutes = {"/api/auth/register", "/api/auth/login"};

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http, AuthManager authManager) {
        return http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers(publicRoutes).permitAll()
                .anyExchange().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) -> {
                    log.error("Unauthorized error");
                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
                })
                .accessDeniedHandler((swe, e) -> {
                    log.error("Access denied");
                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
                })
                .and()
                .addFilterAt(bearerAuthFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }


    private AuthenticationWebFilter bearerAuthFilter(AuthManager authManager){
        AuthenticationWebFilter bearerAuthFilter = new AuthenticationWebFilter(authManager);
        bearerAuthFilter.setServerAuthenticationConverter(
                new BearerTokenServerAuthConverter(new TokenAuthProvider(secret)));
        bearerAuthFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers("/**"));
        return bearerAuthFilter;
    }
}