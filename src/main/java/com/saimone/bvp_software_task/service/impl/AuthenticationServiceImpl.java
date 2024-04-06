package com.saimone.bvp_software_task.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saimone.bvp_software_task.dto.request.EntryRequest;
import com.saimone.bvp_software_task.dto.response.AuthenticationResponse;
import com.saimone.bvp_software_task.handler.ResponseHandler;
import com.saimone.bvp_software_task.model.Role;
import com.saimone.bvp_software_task.model.Token;
import com.saimone.bvp_software_task.model.TokenType;
import com.saimone.bvp_software_task.model.User;
import com.saimone.bvp_software_task.repository.TokenRepository;
import com.saimone.bvp_software_task.repository.UserRepository;
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
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

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
                    .build();

            var savedUser = userRepository.save(userModel);
            var jwtToken = jwtService.generateToken(savedUser);
            var refreshToken = jwtService.generateRefreshToken(savedUser);

            saveUserToken(savedUser, jwtToken);
            AuthenticationResponse response = new AuthenticationResponse(savedUser.getId(), savedUser.getEmail(), jwtToken, refreshToken);

            log.info("IN register - user: {} successfully registered with a token: {}", savedUser.getEmail(), jwtToken);
            return ResponseHandler.responseBuilder("User successfully registered", HttpStatus.CREATED, response);
        }
    }

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
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}