package com.alex.inventory.exceptions;

import lombok.Data;


@Data
public class ErrorDetails {

    private String code;
    private String message;

    public ErrorDetails(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
