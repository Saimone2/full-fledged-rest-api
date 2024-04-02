package com.saimone.bvp_software_task.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping("/current-user")
    public ResponseEntity<Object> currentUser() {
        return null;
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllUsers() {
        return null;
    }
}