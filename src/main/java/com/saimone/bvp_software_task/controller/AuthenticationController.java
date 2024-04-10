package com.saimone.bvp_software_task.controller;

import com.saimone.bvp_software_task.dto.request.EntryRequest;
import com.saimone.bvp_software_task.service.impl.AuthenticationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<Object> registration(@RequestBody EntryRequest request) {
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> authenticate(@RequestBody EntryRequest request) {
        return authenticationService.login(request);
    }

    @GetMapping("/resend/email-confirmation/{email}")
    public ResponseEntity<Object> resendEmailConfirm(@PathVariable String email) {
        return authenticationService.resendEmailConfirm(email);
    }

    @GetMapping("/email-confirm/{token}")
    public ResponseEntity<Object> confirmEmail(@PathVariable String token) {
        return authenticationService.confirmEmail(token);
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