package com.mehmetozanguven.inghubs_digital_wallet.customer;

import com.mehmetozanguven.inghubs_digital_wallet.core.BaseApplicationModuleTest;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.test.ApplicationModuleTest;


@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, extraIncludes = "security")
public class CustomerModuleTest extends BaseApplicationModuleTest {
    private static final String VALID_TCKN = "10000000146";

    @Test
    void doRegisterWithEmailPassword_ShouldRegisterNewCustomer() {
        var request = new RegisterWithEmailPasswordRequest()
                .email("test@gmail.com")
                .password("123456")
                .name("test")
                .surname("surname")
                .tckn(VALID_TCKN)
                ;

        var registerResponseEntity = testRestTemplate.postForEntity(generateUri("/api/auth/customer/create"), request, RegisterApiResponse.class);
        RegisterApiResponse registerApiResponse = registerResponseEntity.getBody();

        Assertions.assertNotNull(registerApiResponse);

        Assertions.assertAll(
                () -> Assertions.assertTrue(registerApiResponse.getIsSuccess()),
                () -> Assertions.assertEquals("test@gmail.com", registerApiResponse.getResponse().getEmail())
        );
    }
}
