package com.saimone.bvp_software_task.controller;

import com.saimone.bvp_software_task.dto.request.UserEntryRequest;
import com.saimone.bvp_software_task.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> registration(@RequestBody UserEntryRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserEntryRequest request) {
        return userService.login(request);
    }

    @GetMapping("/resend/email-confirmation/{email}")
    public ResponseEntity<Object> resendEmailConfirm(@PathVariable String email) {
        return null;
    }

    @GetMapping("/email-confirm/{token}")
    public ResponseEntity<Object> confirmEmail(@PathVariable String token) {
        return null;
    }

    @GetMapping("/send/reset-password-email/{email}")
    public ResponseEntity<Object> resetPasswordEmail(@PathVariable String email) {
        return null;
    }

    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword() {
        return null;
    }
}