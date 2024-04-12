package com.saimone.bvp_software_task.controller;

import com.saimone.bvp_software_task.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('USER')")
    @GetMapping("/current-user")
    public ResponseEntity<Object> currentUser(@RequestHeader("Authorization") String bearerToken) {
        return userService.getCurrentUser(bearerToken);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllUsers(
            @RequestParam(name="page", required=false, defaultValue = "1") Integer page,
            @RequestParam(name="size", required=false, defaultValue = "100") Integer size
    ) {
        return userService.getAllUsers(page, size);
    }
}