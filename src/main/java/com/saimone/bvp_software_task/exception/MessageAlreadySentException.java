package com.saimone.bvp_software_task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MessageAlreadySentException extends RuntimeException {
    public MessageAlreadySentException(String message) {
        super(message);
    }
}