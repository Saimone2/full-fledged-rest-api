package com.saimone.full_fledged_rest_api.controller;

import com.saimone.full_fledged_rest_api.handler.ResponseHandler;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Ping", description = "Checking connection")
public class PingController {

    @GetMapping("/ping")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "The server sends responses",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(type = "string", example = "Pong"))
                    })
    })
    public ResponseEntity<Object> ping() {
        return ResponseHandler.responseBuilder("Pong", HttpStatus.OK);
    }
}