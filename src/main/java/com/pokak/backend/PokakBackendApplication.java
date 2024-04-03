package com.pokak.backend;

import com.pokak.backend.config.AppProperties;
import com.pokak.backend.entity.auth.AuthProvider;
import com.pokak.backend.entity.auth.JwtToken;
import com.pokak.backend.entity.auth.TokenType;
import com.pokak.backend.entity.common.FileDb;
import com.pokak.backend.entity.common.FileType;
import com.pokak.backend.entity.user.Role;
import com.pokak.backend.entity.user.User;
import com.pokak.backend.repository.FileDbRepository;
import com.pokak.backend.repository.UserRepository;
import com.pokak.backend.service.auth.TokenService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
@EnableWebMvc
public class PokakBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokakBackendApplication.class, args);
    }

    @Bean
    @Profile("local")
    public ApplicationRunner initializer
            (
                    FileDbRepository fileDbRepository,
                    UserRepository userRepository,
                    PasswordEncoder passwordEncoder,
                    TokenService tokenService,
                    ResourceLoader resourceLoader

            ) {
        return args -> {
            List<JwtToken> tokens = new LinkedList<>();
            List<FileDb> files = new LinkedList<>();
            List<User> users = new LinkedList<>();
            Random random = new Random();
            for (Integer i = 1; i <= 10; i++) {
                String suffix = i == 1 ? "" : i.toString();
                InputStream inputStream = resourceLoader.getResource("classpath:images\\blank-profile-picture.png").getInputStream();
                FileDb fileDb = new FileDb("blank-profile-picture.png", FileType.IMAGE_PNG, inputStream.readAllBytes());
                fileDb.setId((long) i);
                files.add(fileDb);
                User user = new User();
                user.setId((long) i);
                user.setEmailVerified(false);
                user.setName("Test" + suffix);
                user.setEmail("test@gmail.com" + suffix);
                user.setAuthProvider(AuthProvider.local);
                user.setPassword(passwordEncoder.encode("test" + suffix));
                user.setTwoFactorEnabled(false);
                user.setEmailVerified(true);
                user.setProfileImage(fileDb);
                if (i % 2 == 0) {
                    user.setRole(Role.ADMIN);
                } else {
                    user.setRole(Role.USER);
                }
                users.add(user);
                userRepository.saveAll(users);

                for (Integer j = 1; j <= 1000; j++) {
                    tokenService.createToken(user, Duration.of(0L, ChronoUnit.MILLIS), TokenType.values()[random.nextInt(TokenType.values().length)]);
                }

            }
            fileDbRepository.saveAll(files);

        };
    }
}
