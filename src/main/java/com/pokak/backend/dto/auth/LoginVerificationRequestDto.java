package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginVerificationRequestDto {

    @Email(message = "email.invalidFormat")
    private String email;

    @NotBlank(message = "password.blank")
    private String password;


    private Boolean rememberMe;

    @NotBlank(message = "verificationCode.blank")
    private String code;
}
