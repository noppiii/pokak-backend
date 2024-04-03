package com.pokak.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFactorSetupDto {

    private byte[] qrData;
    private String mimeType;
}