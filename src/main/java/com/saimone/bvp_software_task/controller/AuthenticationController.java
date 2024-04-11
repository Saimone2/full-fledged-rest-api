package com.saimone.bvp_software_task.controller;

import com.saimone.bvp_software_task.dto.request.EntryRequest;
import com.saimone.bvp_software_task.handler.ValidationHandler;
import com.saimone.bvp_software_task.service.impl.AuthenticationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<Object> registration(@Valid @RequestBody EntryRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationHandler.handleValidationErrors(bindingResult);
        }
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> authenticate(@Valid @RequestBody EntryRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationHandler.handleValidationErrors(bindingResult);
        }
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
        return authenticationService.resetPasswordEmail(email);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword(
            @RequestParam(name="email") String email,
            @RequestParam(name="token") String token
    ) {
        return authenticationService.changePassword(email, token);
    }
}