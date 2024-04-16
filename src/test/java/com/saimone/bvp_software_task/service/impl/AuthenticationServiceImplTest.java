package com.saimone.bvp_software_task.service.impl;

import com.saimone.bvp_software_task.dto.request.EntryRequest;
import com.saimone.bvp_software_task.dto.request.ResetPasswordRequest;
import com.saimone.bvp_software_task.dto.response.AuthenticationResponse;
import com.saimone.bvp_software_task.exception.ConfirmationException;
import com.saimone.bvp_software_task.exception.InvalidTokenException;
import com.saimone.bvp_software_task.exception.UserAlreadyExistsException;
import com.saimone.bvp_software_task.exception.UserNotFoundException;
import com.saimone.bvp_software_task.model.ConfirmToken;
import com.saimone.bvp_software_task.model.Role;
import com.saimone.bvp_software_task.model.TokenAssignment;
import com.saimone.bvp_software_task.model.User;
import com.saimone.bvp_software_task.repository.ConfirmTokenRepository;
import com.saimone.bvp_software_task.repository.TokenRepository;
import com.saimone.bvp_software_task.repository.UserRepository;
import com.saimone.bvp_software_task.service.MailSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @Mock
    ModelMapper mapper;
    @Mock
    MailSenderService mailSenderService;
    @Mock
    UserRepository userRepository;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtServiceImpl jwtService;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    ConfirmTokenRepository confirmTokenRepository;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Test
    void register_Success() {
        EntryRequest request = new EntryRequest("test@example.com", "password");
        User newUser = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(mapper.map(any(User.class), eq(AuthenticationResponse.class))).thenReturn(new AuthenticationResponse());

        ResponseEntity<Object> responseEntity = authenticationService.register(request);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UserAlreadyExistsExceptionThrown() {
        EntryRequest request = new EntryRequest("existing@example.com", "password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        EntryRequest request = new EntryRequest("test@example.com", "password");
        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .isEnabled(true)
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        String jwt = "jwtToken";
        String refresh = "refreshToken";
        when(jwtService.generateToken(user)).thenReturn(jwt);
        when(jwtService.generateRefreshToken(user)).thenReturn(refresh);
        when(mapper.map(user, AuthenticationResponse.class)).thenReturn(new AuthenticationResponse());

        ResponseEntity<Object> responseEntity = authenticationService.login(request);

        Object responseBody = responseEntity.getBody();
        Map<String, Object> responseMap = (Map<String, Object>) responseBody;
        Object data = Objects.requireNonNull(responseMap).get("data");
        AuthenticationResponse dataMap = (AuthenticationResponse) data;
        Object accessToken = dataMap.getAccessToken();
        Object refreshToken = dataMap.getRefreshToken();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(jwtService, times(1)).generateToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
        verify(mapper, times(1)).map(user, AuthenticationResponse.class);
        assertEquals(jwt, accessToken);
        assertEquals(refresh, refreshToken);
    }

    @Test
    void confirmEmail_Success() {
        String validToken = "valid_token";

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .isEnabled(false)
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();

        ConfirmToken confirmToken = ConfirmToken.builder()
                .token(validToken)
                .tokenAssignment(TokenAssignment.EMAIL_CONFIRMATION)
                .createdAt(Instant.now())
                .expired(false)
                .revoked(false)
                .build();

        authenticationService.setConfirmTokenLifetime(10);
        when(confirmTokenRepository.findByToken(validToken)).thenReturn(Optional.of(confirmToken));
        when(confirmTokenRepository.findUserByConfirmToken(validToken)).thenReturn(Optional.of(user));

        when(jwtService.generateToken(any())).thenReturn("fake_access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("fake_refresh_token");
        when(mapper.map(any(User.class), eq(AuthenticationResponse.class))).thenReturn(new AuthenticationResponse());

        ResponseEntity<Object> responseEntity = authenticationService.confirmEmail(validToken);
        Object responseBody = responseEntity.getBody();
        Map<String, Object> responseMap = (Map<String, Object>) responseBody;

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Email confirmation successful.", Objects.requireNonNull(responseMap).get("message"));

        verify(confirmTokenRepository).save(confirmToken);
        verify(userRepository).save(user);
    }

    @Test
    public void confirmEmail_InvalidTokenExceptionThrown() {
        String invalidToken = "invalid_token";
        when(confirmTokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> authenticationService.confirmEmail(invalidToken));
        assertEquals("The link has either expired or does not exist.", exception.getMessage());

        verify(confirmTokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendEmailConfirm_Success() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(confirmTokenRepository.findByUserAndTokenAssignment(eq(user), eq(TokenAssignment.EMAIL_CONFIRMATION)))
                .thenReturn(List.of());
        when(confirmTokenRepository.findAllValidConfirmTokenByUser(eq(user.getId()))).thenReturn(List.of());
        when(mapper.map(any(User.class), eq(AuthenticationResponse.class))).thenReturn(new AuthenticationResponse());

        ResponseEntity<Object> responseEntity = authenticationService.resendEmailConfirm(email);
        Object responseBody = responseEntity.getBody();
        Map<String, Object> responseMap = (Map<String, Object>) responseBody;

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("You have already confirmed your account.", Objects.requireNonNull(responseMap).get("message"));
    }

    @Test
    void resendEmailConfirm_WhenUserAlreadyEnabled() {
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .isEnabled(true)
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ConfirmationException exception = assertThrows(ConfirmationException.class, () -> authenticationService.resendEmailConfirm(email));

        assertEquals("You have already confirmed your account.", exception.getMessage());
        verify(mapper, never()).map(any(User.class), eq(AuthenticationResponse.class));
        verify(mailSenderService, never()).sendMail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void resendEmailConfirm_OnUnregisteredEmail() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authenticationService.resendEmailConfirm(email));

        assertEquals("First, please register with your email address.", exception.getMessage());
        verify(mapper, never()).map(any(User.class), eq(AuthenticationResponse.class));
        verify(mailSenderService, never()).sendMail(eq(email), anyString(), anyString());
    }

    @Test
    public void resetPasswordEmail_Success() {
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(confirmTokenRepository.findByUserAndTokenAssignmentAndExpiredAndRevoked(any(), any(), anyBoolean(), anyBoolean())).thenReturn(new ArrayList<>());
        when(mapper.map(user, AuthenticationResponse.class)).thenReturn(new AuthenticationResponse());

        ResponseEntity<Object> responseEntity = authenticationService.resetPasswordEmail("test@example.com");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Object responseBody = responseEntity.getBody();
        Map<String, Object> responseMap = (Map<String, Object>) responseBody;

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Please check your email for instructions on how to reset your password.", Objects.requireNonNull(responseMap).get("message"));
    }

    @Test
    public void resetPasswordEmail_WhenUserNotFound() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authenticationService.resetPasswordEmail(email));

        assertEquals("First, please register with your email address.", exception.getMessage());
        verify(mapper, never()).map(any(User.class), eq(AuthenticationResponse.class));
        verify(mailSenderService, never()).sendMail(eq(email), anyString(), anyString());
    }

    @Test
    void changePassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setVerificationToken("validToken");
        request.setNewPassword("newPassword");

        User user = User.builder()
                .email("test@example.com")
                .password("oldPassword")
                .build();

        ConfirmToken confirmToken = ConfirmToken.builder()
                .user(user)
                .tokenAssignment(TokenAssignment.RESET_PASSWORD)
                .createdAt(Instant.now())
                .revoked(false)
                .expired(false)
                .build();

        authenticationService.setConfirmTokenLifetime(10);
        when(confirmTokenRepository.findByToken("validToken")).thenReturn(Optional.of(confirmToken));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        ResponseEntity<Object> response = authenticationService.changePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).save(user);
        verify(confirmTokenRepository, times(1)).save(confirmToken);
    }

    @Test
    void changePassword_WhenInvalidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setVerificationToken("invalidToken");
        when(confirmTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> authenticationService.changePassword(request));
        assertEquals("Invalid or expired token for password reset.", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(confirmTokenRepository, never()).save(any());
    }
}