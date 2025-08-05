package com.mehmetozanguven.inghubs_digital_wallet.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secretKey;
    private Long expirationInMs;
}
