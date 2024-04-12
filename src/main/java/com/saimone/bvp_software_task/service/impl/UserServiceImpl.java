package com.saimone.bvp_software_task.service.impl;

import com.saimone.bvp_software_task.dto.response.UserResponse;
import com.saimone.bvp_software_task.exception.ParameterOutOfBoundsException;
import com.saimone.bvp_software_task.handler.ResponseHandler;
import com.saimone.bvp_software_task.model.Token;
import com.saimone.bvp_software_task.model.User;
import com.saimone.bvp_software_task.repository.TokenRepository;
import com.saimone.bvp_software_task.repository.UserRepository;
import com.saimone.bvp_software_task.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ModelMapper mapper;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    public ResponseEntity<Object> getCurrentUser(String bearerToken) {
        String tokenStr = bearerToken.substring(7);
        Token token = tokenRepository.findByToken(tokenStr).orElseThrow();

        List<Token> tokens = new ArrayList<>();
        tokens.add(token);

        User user = userRepository.findByTokens(tokens).orElseThrow();
        UserResponse response = mapper.map(user, UserResponse.class);

        log.info("IN currentUser - User information with email address: {} has been successfully obtained", user.getEmail());
        return ResponseHandler.responseBuilder("User information has been successfully obtained.", HttpStatus.OK, response);
    }

    @Override
    public ResponseEntity<Object> getAllUsers(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if (page > 0 && size > 0) {
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<User> usersPage = userRepository.findAll(pageable);
            List<User> users = usersPage.getContent();

            List<UserResponse> response = users.stream()
                    .map(user -> mapper.map(user, UserResponse.class))
                    .collect(Collectors.toList());

            log.info("IN getAllUsers - All user information has been successfully obtained");
            return ResponseHandler.responseBuilder("All user information has been successfully obtained.", HttpStatus.OK, response);
        } else {
            log.warn("IN getAllUsers - Attempt to retrieve information for all users with invalid pagination parameters");
            throw new ParameterOutOfBoundsException("Invalid pagination parameters entered.");
        }
    }
}