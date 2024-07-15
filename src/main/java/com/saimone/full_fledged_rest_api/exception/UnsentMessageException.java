package com.saimone.full_fledged_rest_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnsentMessageException extends RuntimeException {
    public UnsentMessageException(String message) {
        super(message);
    }
}
