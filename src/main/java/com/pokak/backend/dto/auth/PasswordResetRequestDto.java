package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PasswordResetRequestDto {
    @Email(message = "email.invalidFormat")
    private String email;

    @NotBlank(message = "password.blank")
    private String password;

    @NotBlank(message = "token.blank")
    private String token;

}