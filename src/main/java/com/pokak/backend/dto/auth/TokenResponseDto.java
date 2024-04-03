package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponseDto {

    public TokenResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    private String accessToken;
}
