package com.saimone.bvp_software_task.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saimone.bvp_software_task.dto.request.EntryRequest;
import com.saimone.bvp_software_task.dto.response.AuthenticationResponse;
import com.saimone.bvp_software_task.handler.ResponseHandler;
import com.saimone.bvp_software_task.model.*;
import com.saimone.bvp_software_task.repository.ConfirmTokenRepository;
import com.saimone.bvp_software_task.repository.TokenRepository;
import com.saimone.bvp_software_task.repository.UserRepository;
import com.saimone.bvp_software_task.service.AuthenticationService;
import com.saimone.bvp_software_task.service.MailSenderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final MailSenderService mailSenderService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final ConfirmTokenRepository confirmTokenRepository;

    @Override
    public ResponseEntity<Object> register(EntryRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isPresent()) {
            log.warn("IN register - trying to register a previously existing email ({})", request.getEmail());
            return ResponseHandler.responseBuilder("This email is already registered", HttpStatus.FORBIDDEN);
        } else {
            var userModel = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            var savedUser = userRepository.save(userModel);
            String link = sendConfirmationEmail(savedUser);

            AuthenticationResponse response = new AuthenticationResponse(savedUser.getId(), savedUser.getEmail(), link);

            log.info("IN register - user: {} successfully registered", savedUser.getEmail());
            return ResponseHandler.responseBuilder("User successfully registered. Please check your email for confirmation instructions.", HttpStatus.CREATED, response);
        }
    }

    @Override
    public ResponseEntity<Object> login(EntryRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            log.warn("IN login - trying to log in with an email ({}) that is not registered", request.getEmail());
            return ResponseHandler.responseBuilder("Invalid email address or password", HttpStatus.NOT_FOUND);
        } else {
            User userModel = user.get();
            var jwtToken = jwtService.generateToken(userModel);
            var refreshToken = jwtService.generateRefreshToken(userModel);
            revokeAllUserTokens(userModel);

            saveUserToken(userModel, jwtToken);
            AuthenticationResponse response = new AuthenticationResponse(userModel.getId(), userModel.getEmail(), jwtToken, refreshToken);

            log.info("IN login - user: {} successfully logged in with a token: {}", userModel.getEmail(), jwtToken);
            return ResponseHandler.responseBuilder("User successfully logged in", HttpStatus.OK, response);
        }
    }

    private String sendConfirmationEmail(User user) {
        String confirmationToken = UUID.randomUUID().toString();
        saveUserConfirmToken(user, confirmationToken, TokenAssignment.EMAIL_CONFIRMATION);

        String confirmationLink = "http://localhost:8080/api/auth/email-confirm/" + confirmationToken;
        String message = "Please click the link below to confirm your account:\n" + confirmationLink + "\nIf you didn't register an account, just ignore this message.";

        // mailSenderService.sendMail(user.getEmail(), "Account Confirmation", message);
        return confirmationLink;
    }

    private String sendResetPasswordToEmail(User user) {
        String confirmationToken = UUID.randomUUID().toString();
        saveUserConfirmToken(user, confirmationToken, TokenAssignment.RESET_PASSWORD);

        String confirmationLink = "http://localhost:8080/api/auth/change-password?email=" + user.getEmail() + "&token=" + confirmationToken;
        String message = "You have made a password reset request. Please follow the link to confirm your action:\n" + confirmationLink + "\nIf you have not made any requests, just ignore this message.";

        // mailSenderService.sendMail(user.getEmail(), "Account Confirmation", message);
        return confirmationLink;
    }

    private void saveUserConfirmToken(User user, String confirmationToken, TokenAssignment assignment) {
        var token = ConfirmToken.builder()
                .user(user)
                .token(confirmationToken)
                .expired(false)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .tokenAssignment(assignment)
                .build();
        confirmTokenRepository.save(token);
    }

    private void revokeAllUserConfirmTokens(User user) {
        var validUserTokens = confirmTokenRepository.findAllValidConfirmTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        confirmTokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail).orElseThrow();

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = new AuthenticationResponse(user.getId(), user.getEmail(), accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    @Override
    public ResponseEntity<Object> confirmEmail(String token) {
        Optional<ConfirmToken> optionalToken = confirmTokenRepository.findByToken(token);

        if (optionalToken.isPresent()) {
            ConfirmToken tokenModel = optionalToken.get();

            LocalDateTime now = LocalDateTime.now();
            if (tokenModel.getCreatedAt().plusMinutes(10).isBefore(now)) {
                tokenModel.setExpired(true);
                confirmTokenRepository.save(tokenModel);
            }

            if (!tokenModel.expired && !tokenModel.revoked) {
                User user = confirmTokenRepository.findUserByConfirmToken(tokenModel.getToken()).orElseThrow();
                tokenModel.setExpired(true);
                tokenModel.setRevoked(true);
                user.setEnabled(true);

                var jwtToken = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(user);
                revokeAllUserTokens(user);

                saveUserToken(user, jwtToken);

                AuthenticationResponse response = new AuthenticationResponse(user.getId(), user.getEmail(), jwtToken, refreshToken);

                log.info("IN confirmEmail - email ({}) was successfully confirmed", user.getEmail());
                return ResponseHandler.responseBuilder("Email confirmation successful", HttpStatus.OK, response);
            }
        }
        log.info("IN confirmEmail - a non-existent token ({})  was used during email confirmation", token);
        return ResponseHandler.responseBuilder("Time to confirm the link has expired or link does not exist", HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Object> resendEmailConfirm(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (!user.isEnabled()) {
                revokeAllUserConfirmTokens(user);
                String link = sendConfirmationEmail(user);

                AuthenticationResponse response = new AuthenticationResponse(user.getId(), user.getEmail(), link);

                log.info("IN resendEmailConfirm - successfully resend a confirmation to email: {}", email);
                return ResponseHandler.responseBuilder("You have already confirmed your account.", HttpStatus.OK, response);
            } else {
                log.warn("IN resendEmailConfirm - attempting to activate an already confirmed account with email: {}", email);
                return ResponseHandler.responseBuilder("You have already confirmed your account.", HttpStatus.BAD_REQUEST);
            }
        } else {
            log.warn("IN resendEmailConfirm - attempting to resend account confirmation to an unregistered email: {}", email);
            return ResponseHandler.responseBuilder("First, please register with your email address.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<Object> resetPasswordEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            String link = sendResetPasswordToEmail(user);

            AuthenticationResponse response = new AuthenticationResponse(user.getId(), user.getEmail(), link);

            log.info("IN resetPasswordEmail - successful sending of the password reset to email: {}", email);
            return ResponseHandler.responseBuilder("Please check your email for instructions to reset your password.", HttpStatus.OK, response);
        } else {
            log.warn("IN resetPasswordEmail - attempting to reset account password to an unregistered email: {}", email);
            return ResponseHandler.responseBuilder("First, please register with your email address.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<Object> changePassword(String email, String token) {
        log.info("IN changePassword - the password was successfully changed on account with email: {}", email);
        return ResponseHandler.responseBuilder("Password successfully reset. Now you can log in with your new password.", HttpStatus.OK);
    }
}