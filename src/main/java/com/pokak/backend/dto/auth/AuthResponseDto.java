package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {
    private String accessToken;
    private Boolean twoFactorRequired;
    private String tokenType = "Bearer";
    private String message;

}
