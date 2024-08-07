package com.saimone.full_fledged_rest_api.handler;

import com.saimone.full_fledged_rest_api.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class RestExceptionsHandler {
    @ExceptionHandler(IncorrectLoginDetailsException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(IncorrectLoginDetailsException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ParameterOutOfBoundsException.class)
    public ResponseEntity<Object> handleParameterOutOfBoundsException(ParameterOutOfBoundsException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConfirmationException.class)
    public ResponseEntity<Object> handleConfirmationException(ConfirmationException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MessageAlreadySentException.class)
    public ResponseEntity<Object> handleMessageAlreadySentException(MessageAlreadySentException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.TOO_EARLY);
    }

    @ExceptionHandler(UnsentMessageException.class)
    public ResponseEntity<Object> handleUnsentMessageException(UnsentMessageException ex) {
        return ResponseHandler.responseBuilder(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}