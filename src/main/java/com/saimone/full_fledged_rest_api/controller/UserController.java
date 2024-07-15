package com.saimone.full_fledged_rest_api.controller;

import com.saimone.full_fledged_rest_api.dto.response.UserResponse;
import com.saimone.full_fledged_rest_api.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "User", description = "The User API")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('USER')")
    @GetMapping("/current-user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User information has been successfully obtained.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The user is not allowed to act due to the absence of the Admin role.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Object> currentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All user information was successfully retrieved.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters entered.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User cannot act due to lack of Administrator or User role.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Object> getAllUsers(
            @RequestParam(name="page", required=false, defaultValue = "1") String page,
            @RequestParam(name="size", required=false, defaultValue = "100") String size
    ) {
        return userService.getAllUsers(page, size);
    }
}