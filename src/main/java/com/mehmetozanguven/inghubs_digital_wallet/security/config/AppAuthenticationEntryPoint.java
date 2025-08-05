package com.mehmetozanguven.inghubs_digital_wallet.security.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
public class AppAuthenticationEntryPoint  implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public AppAuthenticationEntryPoint(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info("Allowed only for authenticated request : {}", request.getRequestURL());
        resolver.resolveException(request, response, null, authException);
    }
}