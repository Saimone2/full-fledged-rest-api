package com.saimone.bvp_software_task.service.impl;

import com.saimone.bvp_software_task.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public ResponseEntity<Object> currentUser() {
        return null;
    }

    @Override
    public ResponseEntity<Object> all() {
        return null;
    }
}