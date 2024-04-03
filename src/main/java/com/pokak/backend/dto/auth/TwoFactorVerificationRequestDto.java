package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFactorVerificationRequestDto {
    private String code;

}
