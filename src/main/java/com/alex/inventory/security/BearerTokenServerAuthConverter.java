package com.alex.inventory.security;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class BearerTokenServerAuthConverter implements ServerAuthenticationConverter {

    private final TokenAuthProvider tokenAuthProvider;
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Function<String, Mono<String>> getBearerValue = authValue -> Mono.justOrEmpty(
            authValue.substring(BEARER_PREFIX.length())
    );

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return extractHeader(exchange)
                .flatMap(getBearerValue)
                .flatMap(tokenAuthProvider::check)
                .flatMap(UserAuthBearer::create);
    }

    private Mono<String> extractHeader(ServerWebExchange exchange){
        return Mono.justOrEmpty(exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION));
    }
}
