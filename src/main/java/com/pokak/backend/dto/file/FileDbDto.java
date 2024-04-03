package com.pokak.backend.dto.file;

import lombok.Data;

@Data
public class FileDbDto {

    private String name;
    private String type;
    private byte[] data;
}
