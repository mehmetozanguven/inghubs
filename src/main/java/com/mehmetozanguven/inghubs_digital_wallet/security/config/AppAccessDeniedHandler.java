package com.mehmetozanguven.inghubs_digital_wallet.security.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
public class AppAccessDeniedHandler implements AccessDeniedHandler {
    private final HandlerExceptionResolver resolver;

    public AppAccessDeniedHandler(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("Access is defined for the following request :: {} and accessDeniedException :: {}",  request.getRequestURL(), accessDeniedException.getMessage());
        resolver.resolveException(request, response, null, accessDeniedException);
    }
}

