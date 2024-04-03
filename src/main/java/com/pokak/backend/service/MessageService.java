package com.pokak.backend.service;

import com.pokak.backend.util.CookieUtils;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.Cookie;
import java.util.Locale;
import java.util.Optional;

import static com.pokak.backend.service.auth.CookieOAuth2AuthorizationRequestService.LANGUAGE_COOKIE_NAME;

@Service
public class MessageService {
    private final LocaleResolver acceptHeaderLocaleResolver;
    private final ResourceBundleMessageSource messageSource;

    public MessageService(LocaleResolver acceptHeaderLocaleResolver, ResourceBundleMessageSource messageSource) {
        this.acceptHeaderLocaleResolver = acceptHeaderLocaleResolver;
        this.messageSource = messageSource;
    }

    private Locale getLocaleForCurrentRequest() {
        return Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes::getRequest)
                .map(request -> CookieUtils.getCookie(request, LANGUAGE_COOKIE_NAME)
                        .map(Cookie::getValue)
                        .map(Locale::new)
                        .orElse(acceptHeaderLocaleResolver.resolveLocale(request)))
                .orElse(Locale.getDefault());
    }

    public final String getMessage(String code) {
        return getMessage(code, null);
    }

    public final String getMessage(String code, @Nullable Object[] args) throws NoSuchMessageException {
        return getMessage(code, args, null);
    }

    public final String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage) {
        Locale locale = getLocaleForCurrentRequest();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

}
