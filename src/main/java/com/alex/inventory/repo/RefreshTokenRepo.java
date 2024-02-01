package com.alex.inventory.repo;

import com.alex.inventory.entity.RefreshToken;
import com.alex.inventory.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.sql.Ref;

@Repository
public interface RefreshTokenRepo extends R2dbcRepository<RefreshToken, Long> {

    Mono<RefreshToken> findRefreshTokenById(Long id);

    Mono<Boolean> existsByUserId(Long id);

    Mono<Void> deleteRefreshTokenByUserId(Long id);

    Mono<Void> deleteRefreshTokenByRefreshToken(String refreshToken);

    Mono<RefreshToken> findRefreshTokenByRefreshTokenAndUserId(String refreshToken, Long userId);
}
