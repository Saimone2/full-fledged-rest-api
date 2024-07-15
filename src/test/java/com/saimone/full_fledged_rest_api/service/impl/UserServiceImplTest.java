package com.saimone.full_fledged_rest_api.service.impl;

import com.saimone.full_fledged_rest_api.dto.response.UserResponse;
import com.saimone.full_fledged_rest_api.model.Token;
import com.saimone.full_fledged_rest_api.model.User;
import com.saimone.full_fledged_rest_api.repository.TokenRepository;
import com.saimone.full_fledged_rest_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private ModelMapper mapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void getCurrentUser_Success() {
        String testToken = "testToken";
        Token token = Token.builder()
                .id(1L)
                .token(testToken)
                .revoked(false)
                .expired(false)
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findByTokens(any())).thenReturn(Optional.of(user));
        when(mapper.map(any(), eq(UserResponse.class))).thenReturn(new UserResponse());

//        ResponseEntity<Object> responseEntity = userService.getCurrentUser("Bearer " + testToken);
//
//        Object responseBody = responseEntity.getBody();
//        Map<String, Object> responseMap = (Map<String, Object>) responseBody;
//
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertEquals("User information has been successfully obtained.", Objects.requireNonNull(responseMap).get("message"));
    }

    @Test
    public void getAllUsers_Success() {
        List<User> mockUsers = Arrays.asList(new User(), new User());
        Page<User> mockPage = new PageImpl<>(mockUsers);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        List<UserResponse> mockUserResponses = Arrays.asList(new UserResponse(), new UserResponse());
        when(mapper.map(any(User.class), eq(UserResponse.class))).thenReturn(mockUserResponses.get(0), mockUserResponses.get(1));

        ResponseEntity<Object> responseEntity = userService.getAllUsers("1", "10");
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        List<UserResponse> usersResponse = (List<UserResponse>) Objects.requireNonNull(responseBody).get("data");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("All user information has been successfully obtained.", Objects.requireNonNull(responseBody).get("message"));
        assertNotNull(usersResponse);
        assertEquals(2, usersResponse.size());
    }
}