package com.pokak.backend.service.auth;

import com.pokak.backend.config.security.UserPrincipal;
import com.pokak.backend.config.security.oauth2.user.OAuth2UserInfo;
import com.pokak.backend.config.security.oauth2.user.OAuth2UserInfoFactory;
import com.pokak.backend.entity.auth.AuthProvider;
import com.pokak.backend.entity.common.FileDb;
import com.pokak.backend.entity.common.FileType;
import com.pokak.backend.entity.user.Role;
import com.pokak.backend.entity.user.User;
import com.pokak.backend.exception.OAuth2AuthenticationProcessingException;
import com.pokak.backend.repository.UserRepository;
import com.pokak.backend.service.MessageService;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@Service
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SecretGenerator twoFactorSecretGenerator;
    private final MessageService messageService;

    public OAuth2UserService(UserRepository userRepository, SecretGenerator twoFactorSecretGenerator, MessageService messageService) {
        this.userRepository = userRepository;
        this.twoFactorSecretGenerator = twoFactorSecretGenerator;
        this.messageService = messageService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error while authentication oauth2 user", ex);
            throw new InternalAuthenticationServiceException(messageService.getMessage("somethingWrong"));
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) throws IOException, AuthenticationException, OAuth2AuthenticationProcessingException {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException(messageService.getMessage("emailNotFoundFromO2Auth"));
        }

        Optional<User> userOptional = userRepository.findByProviderId(oAuth2UserInfo.getId()).or(() -> userRepository.findByEmail(oAuth2UserInfo.getEmail()));
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getAuthProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                if (user.getAuthProvider() == AuthProvider.local) {
                    throw new OAuth2AuthenticationProcessingException(messageService.getMessage("alreadyHaveRegularAccountO2AuthTemplate", new Object[]{user.getEmail()}));
                } else {
                    throw new OAuth2AuthenticationProcessingException(messageService.getMessage("alreadyHaveAccountO2AuthTemplate", new Object[]{user.getEmail(), user.getAuthProvider()}));
                }
            }
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) throws IOException {
        User user = new User();
        user.setEmailVerified(true);
        user.setAuthProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        String name = oAuth2UserInfo.getName();
        if (userRepository.existsByName(oAuth2UserInfo.getName())){
            int counter = 1;
            while (userRepository.existsByName(name+counter)){
                ++counter;
            }
            name = name + counter;
        }
        user.setName(name);
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setTwoFactorSecret(twoFactorSecretGenerator.generate());
        user.setTwoFactorEnabled(false);
        user.setRole(Role.USER);
        URL url = new URL(oAuth2UserInfo.getImageUrl());
        FileDb profileImage = new FileDb("profile_image.png", FileType.fromMimeType(url.openConnection().getContentType()).orElse(FileType.IMAGE_PNG), IOUtils.toByteArray(url));
        user.setProfileImage(profileImage);
        return userRepository.save(user);
    }

}
