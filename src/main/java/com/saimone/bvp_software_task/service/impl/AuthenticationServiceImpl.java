package com.saimone.bvp_software_task.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saimone.bvp_software_task.dto.request.EntryRequest;
import com.saimone.bvp_software_task.dto.request.ResetPasswordRequest;
import com.saimone.bvp_software_task.dto.response.AuthenticationResponse;
import com.saimone.bvp_software_task.exception.*;
import com.saimone.bvp_software_task.handler.ResponseHandler;
import com.saimone.bvp_software_task.model.*;
import com.saimone.bvp_software_task.repository.ConfirmTokenRepository;
import com.saimone.bvp_software_task.repository.TokenRepository;
import com.saimone.bvp_software_task.repository.UserRepository;
import com.saimone.bvp_software_task.service.AuthenticationService;
import com.saimone.bvp_software_task.service.MailSenderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ModelMapper mapper;
    private final MailSenderService mailSenderService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final ConfirmTokenRepository confirmTokenRepository;

    @Getter
    @Setter
    @Value("${application.security.confirm-token.lifetime}")
    private int confirmTokenLifetime;

    @Override
    public ResponseEntity<Object> register(EntryRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            User newUser = createUserFromRequest(request);

            var savedUser = userRepository.save(newUser);
            String link = sendConfirmationEmail(savedUser);

            AuthenticationResponse response = mapper.map(newUser, AuthenticationResponse.class);
            response.setLink(link);

            log.info("IN register - The user with email: {} has been successfully registered.", savedUser.getEmail());
            return ResponseHandler.responseBuilder("The user has been registered successfully. Please check your email for instructions on how to confirm your registration.", HttpStatus.CREATED, response);
        } else {
            log.warn("IN register - Attempt to register an existing email: {}", request.getEmail());
            throw new UserAlreadyExistsException("This email is already registered.");
        }
    }

    @Override
    public ResponseEntity<Object> login(EntryRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            log.warn("IN login - Attempt to log in with incorrect credentials to account with email: {}", request.getEmail());
            throw new IncorrectLoginDetailsException("The email or password you have entered is incorrect.");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        AuthenticationResponse response = generateTokensAndProduceResponse(user);

        log.info("IN login - User with email: {} has successfully logged in", user.getEmail());
        return ResponseHandler.responseBuilder("The user has logged in successfully.", HttpStatus.OK, response);
    }

    private AuthenticationResponse generateTokensAndProduceResponse(User user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);

        saveUserToken(user, jwtToken);
        AuthenticationResponse response = mapper.map(user, AuthenticationResponse.class);
        response.setAccessToken(jwtToken);
        response.setRefreshToken(refreshToken);

        return response;
    }

    private User createUserFromRequest(EntryRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
    }

    private String sendConfirmationEmail(User user) {
        String confirmationToken = UUID.randomUUID().toString();

        String confirmationLink = "http://localhost:8080/api/auth/email-confirm/" + confirmationToken;
        String message = "Please click the link below to confirm your account:\n" + confirmationLink + "\nIf you didn't register an account, just ignore this message.";

        mailSenderService.sendMail(user.getEmail(), "Account Confirmation", message);
        saveUserConfirmToken(user, confirmationToken, TokenAssignment.EMAIL_CONFIRMATION);

        return confirmationLink;
    }

    protected String sendResetPasswordToEmail(User user) {
        String confirmationToken = UUID.randomUUID().toString();

        String message = "You have made a password reset request. Here is a token that can be used to reset your password:\n" + confirmationToken + "\nDo not disclose the token to anyone. If you have not made any requests, just ignore this message.";

        mailSenderService.sendMail(user.getEmail(), "Account Confirmation", message);
        saveUserConfirmToken(user, confirmationToken, TokenAssignment.RESET_PASSWORD);

        return confirmationToken;
    }

    private void saveUserConfirmToken(User user, String confirmationToken, TokenAssignment assignment) {
        var token = ConfirmToken.builder()
                .user(user)
                .token(confirmationToken)
                .expired(false)
                .revoked(false)
                .createdAt(Instant.now())
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

                AuthenticationResponse authResponse = mapper.map(user, AuthenticationResponse.class);
                authResponse.setAccessToken(accessToken);
                authResponse.setRefreshToken(refreshToken);

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    @Override
    public ResponseEntity<Object> confirmEmail(String token) {
        Optional<ConfirmToken> optionalToken = confirmTokenRepository.findByToken(token);

        if (optionalToken.isPresent()) {
            ConfirmToken tokenModel = optionalToken.get();

            handleTokenExpiration(tokenModel, Instant.now());

            if (!tokenModel.expired && !tokenModel.revoked && tokenModel.getTokenAssignment() == TokenAssignment.EMAIL_CONFIRMATION) {
                User user = confirmTokenRepository.findUserByConfirmToken(tokenModel.getToken()).orElseThrow();
                tokenModel.setExpired(true);
                tokenModel.setRevoked(true);
                user.setEnabled(true);

                confirmTokenRepository.save(tokenModel);
                userRepository.save(user);

                AuthenticationResponse response = generateTokensAndProduceResponse(user);

                log.info("IN confirmEmail - Email: {} was successfully confirmed", user.getEmail());
                return ResponseHandler.responseBuilder("Email confirmation successful.", HttpStatus.OK, response);
            }
        }
        log.warn("IN confirmEmail - An invalid token was used: {} during email confirmation", token);
        throw new InvalidTokenException("The link has either expired or does not exist.");
    }

    void handleTokenExpiration(ConfirmToken tokenModel, Instant now) {
        if (tokenModel.getCreatedAt().plusMillis(confirmTokenLifetime * 60 * 1000L).isBefore(now)) {
            tokenModel.setExpired(true);
            confirmTokenRepository.save(tokenModel);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> resendEmailConfirm(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (!user.isEnabled()) {
                List<ConfirmToken> existingToken = confirmTokenRepository.findByUserAndTokenAssignment(user, TokenAssignment.EMAIL_CONFIRMATION);
                if (existingToken.size() > 1) {
                    log.warn("IN resetPasswordEmail - Attempting to resend confirmation email for user with email: {}", email);
                    throw new MessageAlreadySentException("A confirmation email has already been sent to this email address. Please check your inbox. If the message has not arrived, try resending it in " + confirmTokenLifetime + " minutes.");
                }

                revokeAllUserConfirmTokens(user);
                String link = sendConfirmationEmail(user);

                AuthenticationResponse response = mapper.map(user, AuthenticationResponse.class);
                response.setLink(link);

                log.info("IN resendEmailConfirm - Successfully resend a confirmation to email: {}", email);
                return ResponseHandler.responseBuilder("You have already confirmed your account.", HttpStatus.OK, response);
            } else {
                log.warn("IN resendEmailConfirm - Attempting to activate an already confirmed account with email: {}", email);
                throw new ConfirmationException("You have already confirmed your account.");
            }
        } else {
            log.warn("IN resendEmailConfirm - Attempting to resend account confirmation to an unregistered email: {}", email);
            throw new UserNotFoundException("First, please register with your email address.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> resetPasswordEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            List<ConfirmToken> existingToken = confirmTokenRepository.findByUserAndTokenAssignmentAndExpiredAndRevoked(user, TokenAssignment.RESET_PASSWORD, false, false);
            if (!existingToken.isEmpty()) {
                log.warn("IN resetPasswordEmail - Attempting to resend reset password email for user with email: {}", email);
                throw new MessageAlreadySentException("A reset password email has already been sent to this email address. Please check your inbox. If the message has not arrived, try resending it in " + confirmTokenLifetime + " minutes.");
            }
            String token = sendResetPasswordToEmail(user);

            AuthenticationResponse response = mapper.map(user, AuthenticationResponse.class);
            response.setConfirmToken(token);

            log.info("IN resetPasswordEmail - The password reset has been successfully sent to email: {}", email);
            return ResponseHandler.responseBuilder("Please check your email for instructions on how to reset your password.", HttpStatus.OK, response);
        } else {
            log.warn("IN resetPasswordEmail - Attempting to reset the account password for an email: {} that is not registered", email);
            throw new UserNotFoundException("First, please register with your email address.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> changePassword(ResetPasswordRequest request) {
        Optional<ConfirmToken> optionalToken = confirmTokenRepository.findByToken(request.getVerificationToken());

        if (optionalToken.isPresent()) {
            ConfirmToken confirmToken = optionalToken.get();
            handleTokenExpiration(confirmToken, Instant.now());

            if (!confirmToken.isExpired() && !confirmToken.isRevoked() && confirmToken.getTokenAssignment() == TokenAssignment.RESET_PASSWORD) {
                User user = confirmToken.getUser();
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                confirmToken.setExpired(true);
                confirmToken.setRevoked(true);
                confirmTokenRepository.save(confirmToken);

                log.info("IN changePassword - Password successfully changed for user with email: {}", user.getEmail());
                return ResponseHandler.responseBuilder("Your password has been successfully reset. You can now log in using your new password.", HttpStatus.OK);
            }
        }
        log.warn("IN changePassword - Invalid token or token assignment: {}", request.getVerificationToken());
        throw new InvalidTokenException("Invalid or expired token for password reset.");
    }
}