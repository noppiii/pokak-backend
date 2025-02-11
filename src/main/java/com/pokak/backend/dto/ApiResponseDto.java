package com.pokak.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDto {
    private boolean success;
    private String message;

    public ApiResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
