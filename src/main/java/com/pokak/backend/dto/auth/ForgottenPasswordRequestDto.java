package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ForgottenPasswordRequestDto {

    @NotBlank(message = "{email.notEmpty}")
    @Email
    private String email;

}
