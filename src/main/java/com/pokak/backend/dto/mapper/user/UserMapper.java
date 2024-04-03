package com.pokak.backend.dto.mapper.user;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.pokak.backend.dto.auth.O2AuthInfoDto;
import com.pokak.backend.dto.mapper.CustomMapper;
import com.pokak.backend.dto.mapper.file.FileDbMapper;
import com.pokak.backend.dto.user.UserDto;
import com.pokak.backend.entity.auth.AuthProvider;
import com.pokak.backend.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapperResolver.class, FileDbMapper.class})
public interface UserMapper extends CustomMapper<UserDto, User> {

    @Override
    @Mapping(source = "dto.email", target = "requestedNewEmail")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "twoFactorEnabled", ignore = true)
    User toEntity(Long id, UserDto dto);

    @Override
    @Mapping(target = "o2AuthInfo", source = "user" ,qualifiedByName = "providerToIsO2AuthAccount")
    UserDto toDto(User user);

    @Named("providerToIsO2AuthAccount")
    default O2AuthInfoDto createO2AuthInfo(User user) {
        if (user.getAuthProvider() != AuthProvider.local){
            O2AuthInfoDto o2AuthInfoDto = new O2AuthInfoDto();
            o2AuthInfoDto.setNeedToSetPassword(StringUtils.isBlank(user.getPassword()));
            return o2AuthInfoDto;
        }
        return null;
    }
}
