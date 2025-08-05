package com.mehmetozanguven.inghubs_digital_wallet.security.config;


import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerExternalService;
import com.mehmetozanguven.inghubs_digital_wallet.security.AppUserDetailsService;
import com.mehmetozanguven.inghubs_digital_wallet.security.EndpointInfo;
import com.mehmetozanguven.inghubs_digital_wallet.security.filter.AuthTokenFilter;
import com.mehmetozanguven.inghubs_digital_wallet.security.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final CustomerExternalService userGetService;
    private final JwtProperties jwtProperties;

    public SecurityConfiguration(
            @Qualifier("handlerExceptionResolver")HandlerExceptionResolver handlerExceptionResolver,
            CustomerExternalService userCacheServiceImpl, JwtProperties jwtProperties) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.userGetService = userCacheServiceImpl;
        this.jwtProperties = jwtProperties;
    }


    public static List<PathPatternRequestMatcher> ALLOWED_REQUEST_MATCHERS = new ArrayList<>();
    static {
        ALLOWED_REQUEST_MATCHERS.addAll(EndpointInfo.toPathPatternRequests(EndpointInfo.ALLOWED_ENDPOINTS()));
    }

    public static String[] onlyEmployeesPath() {
        return new String[] {
                "/api/employee/**"
        };
    }

    public static String[] customerPath() {
        return new String[] {
                 "/api/customer/**"
        };
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtProperties.getExpirationInMs(), jwtProperties.getSecretKey());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new AppUserDetailsService(userGetService);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setMaxAge(Duration.ofMinutes(10));
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter(jwtUtil(), userDetailsService());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(httpSecurityCorsConfigurer -> {});
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(httpSecuritySessionManagementConfigurer -> {
            httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });

        // permit static resources, everyone can access
        http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
            authorizationManagerRequestMatcherRegistry.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
        });

        // permit static urls, everyone can access
        for (PathPatternRequestMatcher each :ALLOWED_REQUEST_MATCHERS) {
            http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                authorizationManagerRequestMatcherRegistry.requestMatchers(each).permitAll();
            });
        }

        http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
            authorizationManagerRequestMatcherRegistry.requestMatchers(onlyEmployeesPath()).hasRole(AppRole.EMPLOYEE.plainRole);
        });

        // customer urls can only be accessed with ROLE_CUSTOMER
        http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
            authorizationManagerRequestMatcherRegistry.requestMatchers(customerPath()).hasAnyRole(AppRole.CUSTOMER.plainRole, AppRole.EMPLOYEE.plainRole);
        });


        // other than the static urls, everyone must be logged-in
        http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
            authorizationManagerRequestMatcherRegistry.anyRequest().authenticated();
        });

        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
           httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(new AppAccessDeniedHandler(handlerExceptionResolver));
           httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(new AppAuthenticationEntryPoint(handlerExceptionResolver));
        });


        // add jwt filter
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
