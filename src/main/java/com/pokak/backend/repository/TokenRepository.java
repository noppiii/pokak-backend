package com.pokak.backend.repository;

import com.pokak.backend.entity.auth.JwtToken;
import com.pokak.backend.entity.auth.TokenType;
import com.pokak.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByUserAndTokenType(User user, TokenType tokenType);
    Optional<JwtToken> findByValueAndTokenType(String value, TokenType tokenType);
}
