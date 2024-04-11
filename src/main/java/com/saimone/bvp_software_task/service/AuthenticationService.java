package com.saimone.bvp_software_task.service;

import com.saimone.bvp_software_task.dto.request.EntryRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AuthenticationService {
    ResponseEntity<Object> register(EntryRequest request);
    ResponseEntity<Object> login(EntryRequest request);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
    ResponseEntity<Object> confirmEmail(String token);
    ResponseEntity<Object> resendEmailConfirm(String email);
    ResponseEntity<Object> resetPasswordEmail(String email);
    ResponseEntity<Object> changePassword(String email, String token);
}
