package com.mehmetozanguven.inghubs_digital_wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.BaseSpringBootTest;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.LoginApiResponse;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.LoginRequest;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class ValidationIntegrationTest extends BaseSpringBootTest {

    @Test
    void doLogin_ShouldThrowException_WhenValidationFails() throws Exception {
        LoginRequest loginRequest = new LoginRequest()
                .email(null).password("1234");
        var loginApiResponse = testRestTemplate.postForEntity(generateUri("/api/auth/login"), loginRequest, LoginApiResponse.class);
        int x = 3;

    }
}
