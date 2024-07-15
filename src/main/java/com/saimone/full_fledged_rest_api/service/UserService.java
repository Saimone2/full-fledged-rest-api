package com.saimone.full_fledged_rest_api.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;


public interface UserService {
    ResponseEntity<Object> getCurrentUser(Authentication authentication);
    ResponseEntity<Object> getAllUsers(String page, String size);
}