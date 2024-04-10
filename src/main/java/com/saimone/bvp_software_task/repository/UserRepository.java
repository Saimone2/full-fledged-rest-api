package com.saimone.bvp_software_task.repository;

import com.saimone.bvp_software_task.model.ConfirmToken;
import com.saimone.bvp_software_task.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
