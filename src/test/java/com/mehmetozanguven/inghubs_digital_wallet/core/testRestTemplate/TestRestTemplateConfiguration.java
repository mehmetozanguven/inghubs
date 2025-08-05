package com.mehmetozanguven.inghubs_digital_wallet.core.testRestTemplate;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;

public class TestRestTemplateConfiguration {


    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate();
    }
}
