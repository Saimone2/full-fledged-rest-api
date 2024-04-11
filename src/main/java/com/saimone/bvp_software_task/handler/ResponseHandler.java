package com.saimone.bvp_software_task.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
    public static ResponseEntity<Object> responseBuilder(String message, HttpStatus httpStatus, Object responseObject) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", httpStatus.value());
        response.put("message", message);
        response.put("data", responseObject);
        return new ResponseEntity<>(response, httpStatus);
    }

    public static ResponseEntity<Object> responseBuilder(String message, HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", httpStatus.value());
        response.put("message", message);
        return new ResponseEntity<>(response, httpStatus);
    }
}