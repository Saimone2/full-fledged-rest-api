package com.saimone.bvp_software_task.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationHandler {
    public static ResponseEntity<Object> handleValidationErrors(BindingResult bindingResult) {
        List<FieldError> errors = bindingResult.getFieldErrors();

        Map<String, String> errorMap = new HashMap<>();
        for (FieldError error : errors) {
            errorMap.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseHandler.responseBuilder(errorMap.values().stream().findFirst().orElseThrow(), HttpStatus.FORBIDDEN);
    }
}
