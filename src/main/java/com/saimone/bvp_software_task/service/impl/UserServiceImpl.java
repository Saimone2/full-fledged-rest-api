package com.saimone.bvp_software_task.service.impl;

import com.saimone.bvp_software_task.dto.request.UserEntryRequest;
import com.saimone.bvp_software_task.dto.response.ResponseHandler;
import com.saimone.bvp_software_task.dto.response.UserResponse;
import com.saimone.bvp_software_task.model.User;
import com.saimone.bvp_software_task.repository.UserRepository;
import com.saimone.bvp_software_task.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<Object> register(UserEntryRequest request) {
        Optional<User> user = userRepository.findUserByEmail(request.getEmail());
        if (user.isPresent()) {
            logger.warn("IN register - trying to register a previously existing email ({})", request.getEmail());
            return ResponseHandler.responseBuilder("This email is already registered", HttpStatus.FORBIDDEN);
        } else {
            User newUser = new User();
            newUser.setEmail(request.getEmail());

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            newUser.setPassword(encodedPassword);

            User savedUser = userRepository.save(newUser);
            UserResponse response = new UserResponse(savedUser.getId(), savedUser.getEmail());

            logger.info("IN register - user ({}) successfully registered", savedUser.getEmail());
            return ResponseHandler.responseBuilder("User successfully registered", HttpStatus.CREATED, response);
        }
    }

    @Override
    public ResponseEntity<Object> login(UserEntryRequest request) {
        Optional<User> userOptional = userRepository.findUserByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            logger.warn("IN login - trying to log in with an email ({}) that is not registered", request.getEmail());
            return ResponseHandler.responseBuilder("Invalid email address or password", HttpStatus.FORBIDDEN);
        } else {
            User userModel = userOptional.get();
            if (passwordEncoder.matches(request.getPassword(), userModel.getPassword())) {
                logger.info("IN login - user ({}) successfully logged in", userModel.getEmail());
                return ResponseHandler.responseBuilder("User successfully logged in", HttpStatus.OK);
            } else {
                logger.warn("IN login - trying to log in with invalid password for email ({})", request.getEmail());
                return ResponseHandler.responseBuilder("Invalid email address or password", HttpStatus.FORBIDDEN);
            }
        }
    }
}