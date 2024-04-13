package com.saimone.bvp_software_task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IncorrectLoginDetailsException extends RuntimeException {
    public IncorrectLoginDetailsException(String message) {
        super(message);
    }
}