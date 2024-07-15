package com.saimone.full_fledged_rest_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IncorrectLoginDetailsException extends RuntimeException {
    public IncorrectLoginDetailsException(String message) {
        super(message);
    }
}