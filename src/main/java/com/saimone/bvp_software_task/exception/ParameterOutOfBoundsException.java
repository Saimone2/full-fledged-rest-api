package com.saimone.bvp_software_task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ParameterOutOfBoundsException extends RuntimeException {
    public ParameterOutOfBoundsException(String message) {
        super(message);
    }
}