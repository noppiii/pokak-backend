package com.pokak.backend.service.auth;

import com.pokak.backend.config.AppProperties;
import com.pokak.backend.config.security.UserPrincipal;
import com.pokak.backend.dto.auth.AuthResponseDto;
import com.pokak.backend.dto.auth.LoginRequestDto;
import com.pokak.backend.dto.auth.LoginVerificationRequestDto;
import com.pokak.backend.entity.auth.JwtToken;
import com.pokak.backend.entity.auth.TokenType;
import com.pokak.backend.entity.user.User;
import com.pokak.backend.exception.BadRequestException;
import com.pokak.backend.repository.TokenRepository;
import com.pokak.backend.repository.TwoFactoryRecoveryCodeRepository;
import com.pokak.backend.repository.UserRepository;
import com.pokak.backend.service.MessageService;
import com.pokak.backend.service.user.UserService;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

@Service
@Transactional
public class AuthenticationService {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "rt_cookie";
    private final UserRepository userRepository;
    private final TwoFactoryRecoveryCodeRepository twoFactoryRecoveryCodeRepository;
    private final TokenService tokenService;
    private final AppProperties appProperties;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final MessageService messageService;

    public AuthenticationService(UserRepository userRepository, TwoFactoryRecoveryCodeRepository twoFactoryRecoveryCodeRepository, TokenService tokenService, AppProperties appProperties, AuthenticationManager authenticationManager, UserService userService, TokenRepository tokenRepository, MessageService messageService) {
        this.userRepository = userRepository;
        this.twoFactoryRecoveryCodeRepository = twoFactoryRecoveryCodeRepository;
        this.tokenService = tokenService;
        this.appProperties = appProperties;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.messageService = messageService;
    }

    private boolean isVerificationCodeValid(Long userId, String verificationCode) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("userNotFound"));
        return isVerificationCodeValid(user, verificationCode);
    }

    private boolean isVerificationCodeValid(User user, String verificationCode) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(user.getTwoFactorSecret(), verificationCode);
    }

    private boolean isRecoveryCodeValid(Long userId, String recoveryCode) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("userNotFound"));
        return isRecoveryCodeValid(user, recoveryCode);
    }

    private boolean isRecoveryCodeValid(User user, String recoveryCode) {
        return user.getTwoFactorRecoveryCodes()
                .stream()
                .anyMatch(twoFactorRecoveryCode -> recoveryCode.equals(twoFactorRecoveryCode.getRecoveryCode()));
    }

    private void deleteRecoveryCode(Long userId, String recoveryCode) {
        twoFactoryRecoveryCodeRepository.deleteByUserIdAndRecoveryCode(userId, recoveryCode);
    }

    public AuthResponseDto loginWithVerificationCode(UserPrincipal userPrincipal, String code) {
        User user = userService.findById(userPrincipal.getId()).orElseThrow(() -> new BadRequestException("userNotFound"));
        if (isVerificationCodeValid(userPrincipal.getId(), code)) {
            return getAuthResponse(user);
        }
        throw new BadRequestException("invalidVerificationCode");
    }

    public AuthResponseDto loginWithVerificationCode(LoginVerificationRequestDto loginVerificationRequestDto) {
        UserPrincipal userPrincipal = getUserPrincipal(loginVerificationRequestDto.getEmail(), loginVerificationRequestDto.getPassword());
        return loginWithVerificationCode(userPrincipal, loginVerificationRequestDto.getCode());
    }

    public AuthResponseDto loginWithRecoveryCode(UserPrincipal userPrincipal, String verificationCode) {
        User user = userService.findByEmail(userPrincipal.getEmail()).orElseThrow(() -> new BadRequestException("userNotFound"));
        if (isRecoveryCodeValid(user.getId(), verificationCode)) {
            deleteRecoveryCode(user.getId(), verificationCode);
            return getAuthResponse(user);
        }
        throw new BadRequestException("invalidRecoveryCode");
    }

    public AuthResponseDto loginWithRecoveryCode(LoginVerificationRequestDto loginVerificationRequestDto) {
        UserPrincipal userPrincipal = getUserPrincipal(loginVerificationRequestDto.getEmail(), loginVerificationRequestDto.getPassword());
        return loginWithRecoveryCode(userPrincipal, loginVerificationRequestDto.getCode());
    }

    public AuthResponseDto login(UserPrincipal userPrincipal) {
        User user = userService.findByEmail(userPrincipal.getEmail()).orElseThrow(() -> new BadRequestException("userNotFound"));
        if (user.getEmailVerified()) {
            return getAuthResponse(user);
        }
        throw new BadRequestException("accountNotActivated");
    }

    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        UserPrincipal userPrincipal = getUserPrincipal(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return login(userPrincipal);
    }

    public Optional<JwtToken> getRefreshToken() {
        HttpServletRequest request = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes::getRequest).orElseThrow(IllegalStateException::new);
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                    .findFirst()
                    .flatMap(cookie -> tokenRepository.findByValueAndTokenType(cookie.getValue(), TokenType.REFRESH));
        }
        return Optional.empty();
    }

    public String createAccessToken(User user) {
        return tokenService.createJwtTokenValue(user.getId(), Duration.of(appProperties.getAuth().getAccessTokenExpirationMsec(), ChronoUnit.MILLIS));
    }

    private JwtToken createRefreshToken(User user) {
        return tokenService.createToken(user, Duration.of(appProperties.getAuth().getRefreshTokenExpirationMsec(), ChronoUnit.MILLIS), TokenType.REFRESH);
    }


    private void addRefreshToken(User user) {
        JwtToken refreshToken = createRefreshToken(user);
        HttpServletResponse response = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes::getResponse).orElseThrow(IllegalStateException::new);
        Date expires = new Date();
        expires.setTime(expires.getTime() + appProperties.getAuth().getRefreshTokenExpirationMsec());
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        response.setHeader("Set-Cookie", String.format("%s=%s; Expires=%s; Path=/; HttpOnly; SameSite=none; Secure", REFRESH_TOKEN_COOKIE_NAME, refreshToken.getValue(), df.format(expires)));
    }

    public void removeRefreshToken() {
        HttpServletResponse response = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes::getResponse).orElseThrow(IllegalStateException::new);
        Date expires = new Date();
        expires.setTime(expires.getTime() + 1);
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        response.setHeader("Set-Cookie", String.format("%s=; Expires=%s; Path=/; HttpOnly; SameSite=none; Secure", REFRESH_TOKEN_COOKIE_NAME, df.format(expires)));
    }

    public void logout(User user) {
        Optional<JwtToken> optionalRefreshToken = getRefreshToken();
        if (optionalRefreshToken.isPresent() && optionalRefreshToken.get().getUser().getId().equals(user.getId())) {
            tokenService.delete(optionalRefreshToken.get());
            removeRefreshToken();
        } else {
            throw new BadRequestException("tokenExpired");
        }

    }

    private UserPrincipal getUserPrincipal(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        return (UserPrincipal) authentication.getPrincipal();
    }

    private AuthResponseDto getAuthResponse(User user) {
        String accessToken = createAccessToken(user);
        AuthResponseDto authResponseDto = new AuthResponseDto();
        authResponseDto.setTwoFactorRequired(user.getTwoFactorEnabled());
        authResponseDto.setAccessToken(accessToken);
        addRefreshToken(user);
        return authResponseDto;
    }

}
