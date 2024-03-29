package com.alex.inventory.service;


import com.alex.inventory.dto.AuthRequestDto;
import com.alex.inventory.dto.AuthResponseDto;
import com.alex.inventory.entity.UserEntity;
import com.alex.inventory.repo.UserRepo;
import com.alex.inventory.security.TokenDetails;
import com.alex.inventory.util.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public Mono<UserEntity> registerUser(UserEntity userEntity){
        return userRepo.save(
                userEntity.toBuilder()
                        .password(passwordEncoder.encode(userEntity.getPassword()))
                        .role(userEntity.getRole())
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        ).doOnSuccess(u -> log.info("IN registerUser  - user: {} created", u));


    }

    public AuthResponseDto createAuthResponseDto(TokenDetails tokenDetails){
         return AuthResponseDto.builder().userId(tokenDetails.getUserId())
                .token(tokenDetails.getToken()).issuedAt(tokenDetails.getIssuedAt())
                .expiresAt(tokenDetails.getExpiresAt()).role(tokenDetails.getRole())
                .refreshToken(tokenDetails.getRefreshToken())
                .build();
    }




    public Mono<Boolean> checkIfUserExistsByUsername(String username){
        return userRepo.existsByUsername(username);
    }

    public Mono<UserEntity> getUserById(Long id){
        return userRepo.findById(id);
    }

    public Mono<UserEntity> getUserByUserName(String username){
        return userRepo.findByUsername(username);
    }
}

