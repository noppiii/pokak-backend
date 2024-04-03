package com.pokak.backend.controller;

import com.pokak.backend.config.AppProperties;
import com.pokak.backend.dto.ApiResponseDto;
import com.pokak.backend.exception.BadRequestException;
import com.pokak.backend.exception.UnauthorizedRequestException;
import com.pokak.backend.repository.FileDbRepository;
import com.pokak.backend.repository.TwoFactoryRecoveryCodeRepository;
import com.pokak.backend.service.EmailService;
import com.pokak.backend.service.FileDbService;
import com.pokak.backend.service.MessageService;
import com.pokak.backend.service.auth.AuthenticationService;
import com.pokak.backend.service.auth.CustomUserDetailsService;
import com.pokak.backend.service.auth.TokenService;
import com.pokak.backend.service.user.UserService;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@Slf4j
public abstract class Controller {


    @Autowired
    protected AuthenticationManager authenticationManager;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected TokenService tokenService;
    @Autowired
    protected EmailService emailService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected FileDbService storageService;
    @Autowired
    protected AppProperties appProperties;
    @Autowired
    protected SecretGenerator twoFactorSecretGenerator;
    @Autowired
    protected CustomUserDetailsService customUserDetailsService;
    @Autowired
    protected TwoFactoryRecoveryCodeRepository twoFactoryRecoveryCodeRepository;
    @Autowired
    protected AuthenticationService authenticationService;
    @Autowired
    protected MessageService messageService;
    @Autowired
    protected FileDbRepository fileDbRepository;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponseDto handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        return new ApiResponseDto(false, ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map((error) -> messageService.getMessage(error.getDefaultMessage()))
                .collect(Collectors.joining(",")));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ApiResponseDto handleBadRequestException(BadRequestException ex) {
        log.error(ex.getMessage());
        return new ApiResponseDto(false, messageService.getMessage(ex.getLocalizedMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = {UnauthorizedRequestException.class, AuthenticationException.class})
    public ApiResponseDto handleUnauthorized() {
        return new ApiResponseDto(false, messageService.getMessage("Email atau Password yang anda masukan tidak sesuai"));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ApiResponseDto handleAnyException(Exception e) {
        log.error("Error while processing exception",e);
        return new ApiResponseDto(false, messageService.getMessage("somethingWrong"));
    }

}
