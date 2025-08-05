package com.mehmetozanguven.inghubs_digital_wallet.core;

import com.mehmetozanguven.inghubs_digital_wallet.core.clearDatabase.ClearDatabaseBeforeEach;
import com.mehmetozanguven.inghubs_digital_wallet.core.testRestTemplate.TestRestTemplateConfiguration;
import com.mehmetozanguven.inghubs_digital_wallet.core.testcontainer.EnableTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableTestcontainers
@ClearDatabaseBeforeEach
@Import(value = {TestRestTemplateConfiguration.class})
public abstract class BaseSpringBootTest {
    @Autowired
    public TestRestTemplate testRestTemplate;
    @LocalServerPort
    public int randomServerPort;

    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort;
    }

    public URI generateUri(String endpoint) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getBaseUrl() + endpoint);
        return builder.build().toUri();
    }
}
