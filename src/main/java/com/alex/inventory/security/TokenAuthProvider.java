package com.alex.inventory.security;


import com.alex.inventory.entity.enums.ErrorCode;
import com.alex.inventory.exceptions.AuthException;
import com.alex.inventory.exceptions.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Date;

public class TokenAuthProvider {
    private final String secret;

    public TokenAuthProvider(String secret) {
        this.secret = secret;
    }

    public Mono<VerificationResult> check(String accessToken){
        return Mono.just(verify(accessToken))
                .onErrorResume(e -> Mono.error(new UnauthorizedException(e.getMessage())));
    }


    private VerificationResult verify(String token){
        Claims claims = getClaimsFromToken(token);
        final Date expirationDate = claims.getExpiration();
        if (expirationDate.before(new Date())){
            throw new AuthException("Token expired", ErrorCode.TOKEN_EXPIRED.name());
        }

        return new VerificationResult(claims, token);

    }
    private Claims getClaimsFromToken(String token){
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .parseClaimsJws(token)
                .getBody();
    }
    public static class VerificationResult{
        public Claims claims;
        public String token;

        public VerificationResult(Claims claims, String token) {
            this.claims = claims;
            this.token = token;
        }
    }

}
