package com.saimone.bvp_software_task.controller;

import com.saimone.bvp_software_task.handler.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class PingController {
    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        return ResponseHandler.responseBuilder("Pong", HttpStatus.OK);
    }
}