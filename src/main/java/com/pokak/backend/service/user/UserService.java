package com.pokak.backend.service.user;

import com.pokak.backend.dto.auth.*;
import com.pokak.backend.dto.user.UserDto;
import com.pokak.backend.entity.user.User;
import dev.samstevens.totp.exceptions.QrGenerationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Optional;

public interface UserService {
    User createNewUser(SignUpRequestDto signUpRequestDto) throws URISyntaxException, IOException;
    User updateUserPassword(User user, String newPassword);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    void cancelUserAccount(Long userId);
    User updatePassword(User user, ChangePasswordDto changePasswordDto);
    User activateUserAccount(TokenAccessRequestDto tokenAccessRequestDto);
    User disableTwoFactorAuthentication(User user);
    User enableTwoFactorAuthentication(User user);
    User activateRequestedEmail(TokenAccessRequestDto tokenAccessRequestDto);
    User updateProfile(Long currentUserId, UserDto newUser) throws URISyntaxException, MalformedURLException;
    User setNewTwoFactorSecret(User user);
    void requestPasswordReset(User user) throws URISyntaxException, MalformedURLException;
    void resetPassword(User user, PasswordResetRequestDto passwordResetRequestDto);
    TwoFactorSetupDto getTwoFactorSetup(User user) throws QrGenerationException;
    TwoFactorDto verifyTwoFactor(User user, String code);
    boolean isUsernameUsed(String username);
    public boolean isEmailUsed(String email);
}
