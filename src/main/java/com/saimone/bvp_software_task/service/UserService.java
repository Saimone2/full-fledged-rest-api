package com.saimone.bvp_software_task.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

public interface UserService {
    ResponseEntity<Object> currentUser();
    ResponseEntity<Object> all();
}