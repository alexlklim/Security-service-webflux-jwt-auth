package com.alex.inventory.controller;

import com.alex.inventory.dto.AuthRequestDto;
import com.alex.inventory.dto.AuthResponseDto;
import com.alex.inventory.dto.UserDto;
import com.alex.inventory.entity.UserEntity;
import com.alex.inventory.entity.enums.ErrorCode;
import com.alex.inventory.exceptions.ExceptionHandler;
import com.alex.inventory.mapper.UserMapper;
import com.alex.inventory.security.CustomPrincipal;
import com.alex.inventory.security.SecurityService;
import com.alex.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {


    private final SecurityService securityService;
    private final UserService userService;
    private final UserMapper userMapper;


    @PostMapping("/register")
    public Mono<?> register(@RequestBody UserDto dto){
        UserEntity userEntity = userMapper.map(dto);
        return userService.checkIfUserExistsByUsername(userEntity.getUsername())
                .flatMap(result -> {
                    if (result) {
                        log.error(ErrorCode.USER_ALREADY_EXISTS_EXCEPTION.name());
                        return Mono.just(
                                new ExceptionHandler(HttpStatus.CONFLICT, ErrorCode.USER_ALREADY_EXISTS_EXCEPTION.toString())
                        );
                    } else {
                        return userService.registerUser(userEntity)
                                .map(userMapper::map);
                    }
                });
    }




    @PostMapping("/login")
    public Mono<AuthResponseDto> login(@RequestBody AuthRequestDto dto){
        return securityService.authenticate(dto.getUsername(), dto.getPassword())
                .flatMap(tokenDetails -> Mono.just(
                        AuthResponseDto.builder()
                                .userId(tokenDetails.getUserId())
                                .token(tokenDetails.getToken())
                                .issuedAt(tokenDetails.getIssuedAt())
                                .expiresAt(tokenDetails.getExpiresAt())
                                .build()
                ));
    }

    @GetMapping("/info")
    public Mono<UserDto> getUserInfo(Authentication authentication){
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        return userService.getUserById(customPrincipal.getId())
                .map(userMapper::map);

    }


}
