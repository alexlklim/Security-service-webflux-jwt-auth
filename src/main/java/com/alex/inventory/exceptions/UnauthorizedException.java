package com.alex.inventory.exceptions;

import com.alex.inventory.entity.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends ApiException{
    public UnauthorizedException(String message) {
        super(message, ErrorCode.UNAUTHORIZED.name());
    }
}
