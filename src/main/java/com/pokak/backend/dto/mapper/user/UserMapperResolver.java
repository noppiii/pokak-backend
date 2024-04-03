package com.pokak.backend.dto.mapper.user;

import com.pokak.backend.entity.user.User;
import com.pokak.backend.exception.BadRequestException;
import com.pokak.backend.repository.UserRepository;
import org.mapstruct.ObjectFactory;
import org.springframework.stereotype.Component;

@Component
public class UserMapperResolver {


    private final UserRepository userRepository;

    public UserMapperResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @ObjectFactory
    public User resolve(Long id){
        return userRepository.findById(id).orElseThrow(()->new BadRequestException("userNotFound"));
    }
}
