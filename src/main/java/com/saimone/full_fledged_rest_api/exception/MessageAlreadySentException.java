package com.saimone.full_fledged_rest_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_EARLY)
public class MessageAlreadySentException extends RuntimeException {
    public MessageAlreadySentException(String message) {
        super(message);
    }
}