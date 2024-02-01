package com.alex.inventory.service;


import com.alex.inventory.dto.RefreshTokenDto;
import com.alex.inventory.entity.RefreshToken;
import com.alex.inventory.entity.UserEntity;
import com.alex.inventory.repo.RefreshTokenRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepo refreshTokenRepo;

    public Mono<RefreshToken> generateRefreshToken(UserEntity userEntity) {
        Long userId = userEntity.getId();

        return refreshTokenRepo.existsByUserId(userId)
                .flatMap(exists -> {
                    if (exists) {
                        log.info("TOKEN FOR THIS USER ALREADY EXISTS");
                        log.info("DELETE TOKEN");
                        return refreshTokenRepo.deleteRefreshTokenByUserId(userId)
                                .then(createNewRefreshToken(userEntity));
                    } else {
                        return createNewRefreshToken(userEntity);
                    }
                });
    }

    private Mono<RefreshToken> createNewRefreshToken(UserEntity userEntity) {
        RefreshToken refreshToken = new RefreshToken();
        return refreshTokenRepo.save(
                refreshToken.toBuilder()
                        .refreshToken(generateUniqueRefreshToken())
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .userId(userEntity.getId())
                        .build()
        ).doOnSuccess(u -> log.info("Refresh token: {} for user: {} created", u.getRefreshToken(), userEntity.getUsername()));
    }



    private String generateUniqueRefreshToken() {
        return UUID.randomUUID().toString();
    }


    public Mono<RefreshToken> getRefreshToken(RefreshTokenDto refreshTokenDto) {
        return refreshTokenRepo.findRefreshTokenByRefreshTokenAndUserId(
                        refreshTokenDto.getRefreshToken(), refreshTokenDto.getUserId());
    }

    public Mono<Void> deleteRefreshToken(RefreshToken refreshToken){
        return refreshTokenRepo.delete(refreshToken);
    }




}
