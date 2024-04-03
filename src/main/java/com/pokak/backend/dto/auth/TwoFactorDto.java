package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TwoFactorDto {
    private List<String> verificationCodes;
}
