package com.saimone.bvp_software_task.service.impl;

import com.saimone.bvp_software_task.dto.response.UserResponse;
import com.saimone.bvp_software_task.handler.ResponseHandler;
import com.saimone.bvp_software_task.model.User;
import com.saimone.bvp_software_task.repository.UserRepository;
import com.saimone.bvp_software_task.service.JwtService;
import com.saimone.bvp_software_task.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<Object> getCurrentUser(String bearerToken) {
        String token = bearerToken.substring(7);
        String email = jwtService.extractUsername(token);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.isEnabled(), user.getCreatedAt());

            log.info("IN currentUser - user information with email: {} successfully received", user.getEmail());
            return ResponseHandler.responseBuilder("User information successfully received", HttpStatus.OK, response);
        } else {
            log.warn("IN currentUser - attempting to obtain information on an incorrect jwt token: {}", bearerToken);
            return ResponseHandler.responseBuilder("Something went wrong...", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public ResponseEntity<Object> getAllUsers(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if(page > 0 && size > 0) {
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<User> usersPage = userRepository.findAll(pageable);
            List<User> users = usersPage.getContent();

            List<UserResponse> userResponse = users.stream()
                    .map(user -> new UserResponse(user.getId(), user.getEmail(), user.isEnabled(), user.getCreatedAt()))
                    .collect(Collectors.toList());

            log.info("IN getAllUsers - information about all users has been obtained");
            return ResponseHandler.responseBuilder("Information about all users has been successfully retrieved", HttpStatus.OK, userResponse);
        } else {
            log.warn("IN getAllUsers - ");
            return ResponseHandler.responseBuilder("Id", HttpStatus.BAD_REQUEST);
        }
    }
}