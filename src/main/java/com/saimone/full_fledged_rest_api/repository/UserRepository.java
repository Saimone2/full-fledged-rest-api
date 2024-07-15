package com.saimone.full_fledged_rest_api.repository;

import com.saimone.full_fledged_rest_api.model.Token;
import com.saimone.full_fledged_rest_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTokens(List<Token> tokens);
}