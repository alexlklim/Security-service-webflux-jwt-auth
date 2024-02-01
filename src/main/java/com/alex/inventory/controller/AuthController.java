package com.alex.inventory.controller;

import com.alex.inventory.dto.*;
import com.alex.inventory.entity.UserEntity;
import com.alex.inventory.entity.enums.ErrorCode;
import com.alex.inventory.exceptions.ErrorDetails;
import com.alex.inventory.mapper.UserMapper;
import com.alex.inventory.security.CustomPrincipal;
import com.alex.inventory.service.SecurityService;
import com.alex.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<?>> register(@RequestBody UserDto dto, Authentication authentication) {

        System.out.println(authentication.getAuthorities());
        if (authentication.getAuthorities().toString().equalsIgnoreCase("[ADMIN]")){
            UserEntity userEntity = userMapper.map(dto);
            return userService.checkIfUserExistsByUsername(userEntity.getUsername())
                    .flatMap(userExists -> {
                        if (userExists) {
                            log.error(ErrorCode.USER_ALREADY_EXISTS_EXCEPTION.name());
                            return Mono.just(new ResponseEntity<>(
                                    new ErrorDetails("409", ErrorCode.USER_ALREADY_EXISTS_EXCEPTION.name()),
                                    HttpStatus.CONFLICT));
                        } else {
                            log.error(ErrorCode.USER_ALREADY_EXISTS_EXCEPTION.name());
                            return userService.registerUser(userEntity)
                                    .map(savedUser -> new ResponseEntity<>(userMapper.map(savedUser), HttpStatus.CREATED));
                        }
                    })
                    .defaultIfEmpty(new ResponseEntity<>(
                            new ErrorDetails("500", ErrorCode.INTERNAL_SERVER_ERROR.name()),
                            HttpStatus.INTERNAL_SERVER_ERROR));
        } else {
            return Mono.just(new ResponseEntity<>(new ErrorDetails("401", ErrorCode.UNAUTHORIZED.name()),
                    HttpStatus.UNAUTHORIZED));
        }

    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponseDto>> login(@RequestBody AuthRequestDto dto) {
        return securityService.authenticate(dto.getUsername(), dto.getPassword())
                .map(tokenDetails -> ResponseEntity.ok(userService.createAuthResponseDto(tokenDetails)
                )).defaultIfEmpty(
                        new ResponseEntity<>(HttpStatus.UNAUTHORIZED)
                );
    }


    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<AuthResponseDto>> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto){
        return securityService.refreshToken(refreshTokenDto).map(tokenDetails ->
                ResponseEntity.ok(userService.createAuthResponseDto(tokenDetails))
        ).defaultIfEmpty(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("/activate/{token}/{email}")
    public Mono<Boolean> activateAccount(@PathVariable("token") String token, @PathVariable("email") String email){


        // check if token is correct

        // check if user with this email exists
        // activate user account



        return Mono.empty();
    }

    @PostMapping("/change-password")
    public Mono<Boolean> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication){
        // check if username and old password is correct
        // change password to new
        // return Ok or Unauthorized
        return Mono.empty();
    }




    @GetMapping("/info")
    public Mono<UserDto> getUserInfo(Authentication authentication){
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        return userService.getUserById(customPrincipal.getId())
                .map(userMapper::map);

    }




}
