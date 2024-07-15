package com.saimone.full_fledged_rest_api.service.impl;

import com.saimone.full_fledged_rest_api.dto.response.UserResponse;
import com.saimone.full_fledged_rest_api.exception.ParameterOutOfBoundsException;
import com.saimone.full_fledged_rest_api.handler.ResponseHandler;
import com.saimone.full_fledged_rest_api.model.User;
import com.saimone.full_fledged_rest_api.repository.UserRepository;
import com.saimone.full_fledged_rest_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ModelMapper mapper;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<Object> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication instanceof UsernamePasswordAuthenticationToken userToken) {
            if (userToken.getPrincipal() instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                Optional<User> user = userRepository.findByEmail(email);
                if(user.isPresent()) {
                    UserResponse response = mapper.map(user.get(), UserResponse.class);

                    log.info("IN currentUser - User information with email address: {} has been successfully obtained", email);
                    return ResponseHandler.responseBuilder("User information has been successfully obtained.", HttpStatus.OK, response);
                }
            }
        }
        return ResponseHandler.responseBuilder("User not authenticated or no email available.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> getAllUsers(String pageStr, String sizeStr) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        try {
            int page = Integer.parseInt(pageStr);
            int size = Integer.parseInt(sizeStr);

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
        } catch (NumberFormatException e) {
            log.warn("IN getAllUsers - Attempt to retrieve information for all users with invalid pagination parameters");
            throw new ParameterOutOfBoundsException("Invalid pagination parameters entered.");
        }
    }
}