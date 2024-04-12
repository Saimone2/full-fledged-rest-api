package com.saimone.bvp_software_task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ConfirmationException extends RuntimeException {
    public ConfirmationException(String message) {
        super(message);
    }
}