package com.pokak.backend.dto.user;

import com.pokak.backend.dto.auth.O2AuthInfoDto;
import com.pokak.backend.dto.file.FileDbDto;
import com.pokak.backend.dto.validation.File;
import com.pokak.backend.entity.common.FileType;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UserDto {

    @Size(min = 4, message = "name.lengthRestriction")
    private  String name;
    @Email(message = "email.invalidFormat")
    private String email;

    @File(maxSizeBytes = 10000000, fileTypes = {FileType.IMAGE_JPEG, FileType.IMAGE_PNG}, message = "profileImage.invalidMessage")
    private FileDbDto profileImage;

    private Boolean twoFactorEnabled;

    private O2AuthInfoDto o2AuthInfo;


}