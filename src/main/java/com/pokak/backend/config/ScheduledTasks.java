package com.pokak.backend.config;

import com.pokak.backend.entity.auth.JwtToken;
import com.pokak.backend.repository.TokenRepository;
import com.pokak.backend.service.auth.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScheduledTasks {
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;

    @Autowired
    public ScheduledTasks(TokenRepository tokenRepository, TokenService tokenService) {
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
    }

    @Scheduled(fixedDelayString = "${app.deleteExpiredTokensDelayMsec}")
    public void deleteExpiredTokens() {
        log.info("Deleting expired tokens");
        List<JwtToken> expiredTokens = tokenRepository
                .findAll()
                .stream()
                .filter(jwtToken -> !tokenService.validateJwtToken(jwtToken.getValue()))
                .collect(Collectors.toList());
        tokenRepository.deleteAll(expiredTokens);
        log.info("Following tokens were deleted {}", expiredTokens);
    }
}