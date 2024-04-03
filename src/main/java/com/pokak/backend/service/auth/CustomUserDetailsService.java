package com.pokak.backend.service.auth;

import com.pokak.backend.config.security.UserPrincipal;
import com.pokak.backend.entity.user.User;
import com.pokak.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser.map(UserPrincipal::create).orElse(null);
    }

    @Transactional
    public Optional<UserDetails> loadUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.map(UserPrincipal::create);
    }
}
