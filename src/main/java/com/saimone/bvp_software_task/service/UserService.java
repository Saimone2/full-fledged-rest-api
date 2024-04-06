package com.saimone.bvp_software_task.service;

import com.saimone.bvp_software_task.dto.request.UserEntryRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<Object> register(UserEntryRequest request);

    ResponseEntity<Object> login(UserEntryRequest request);
}