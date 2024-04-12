package com.saimone.bvp_software_task.service;

import org.springframework.http.ResponseEntity;


public interface UserService {
    ResponseEntity<Object> getCurrentUser(String bearerToken);
    ResponseEntity<Object> getAllUsers(Integer page, Integer size);
}