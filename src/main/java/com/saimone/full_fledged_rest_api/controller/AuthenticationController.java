package com.saimone.full_fledged_rest_api.controller;

import com.saimone.full_fledged_rest_api.dto.request.EntryRequest;
import com.saimone.full_fledged_rest_api.dto.request.ResetPasswordRequest;
import com.saimone.full_fledged_rest_api.dto.response.AuthenticationResponse;
import com.saimone.full_fledged_rest_api.handler.ValidationHandler;
import com.saimone.full_fledged_rest_api.service.impl.AuthenticationServiceImpl;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationService;

    @PostMapping("/signup")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(
                    responseCode = "406",
                    description = "Bad request due to validation errors.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with the provided email already exists.",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Object> registration(@Valid @RequestBody EntryRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationHandler.handleValidationErrors(bindingResult);
        }
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully logged in.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "406",
                    description = "Bad request due to validation errors.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "The email or password you have entered is incorrect.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Object> authenticate(@Valid @RequestBody EntryRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationHandler.handleValidationErrors(bindingResult);
        }
        return authenticationService.login(request);
    }

    @GetMapping("/resend/email-confirmation/{email}")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "A confirmation email is sent to the user's email address.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The user has already verified their account.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "425",
                    description = "The user has already received an email message earlier.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The user has not registered their email address.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "There was an error sending the email.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Object> resendEmailConfirm(@PathVariable String email) {
        return authenticationService.resendEmailConfirm(email);
    }

    @GetMapping("/email-confirm/{token}")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email confirmation successful.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The link has either expired or does not exist.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Object> confirmEmail(@PathVariable String token) {
        return authenticationService.confirmEmail(token);
    }

    @GetMapping("/send/reset-password-email/{email}")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "A password reset message was sent to the user.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The user has not registered their email address.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "425",
                    description = "The user has already received an email message earlier.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "There was an error sending the email.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Object> resetPasswordEmail(@PathVariable String email) {
        return authenticationService.resetPasswordEmail(email);
    }

    @PostMapping("/change-password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "The user password has been successfully reset.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "406",
                    description = "Bad request due to validation errors.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The link has either expired or does not exist.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Object> changePassword(@RequestBody ResetPasswordRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationHandler.handleValidationErrors(bindingResult);
        }
        return authenticationService.changePassword(request);
    }
}