package com.mehmetozanguven.inghubs_digital_wallet.security;

import com.mehmetozanguven.inghubs_digital_wallet.core.BaseSpringBootTest;
import com.mehmetozanguven.inghubs_digital_wallet.core.GenericApiResponse;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletExternalEmployeeService;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

public class EmployeeSecurityIntegrationTest extends BaseSpringBootTest {
    private static final String VALID_TCKN = "10000000146";

    @Autowired
    WalletExternalEmployeeService externalEmployeeService;

    @Test
    @WithMockUser(username = "customer", roles = { "CUSTOMER" })
    void shouldThrowError_WhenCustomerRunsSecuredMethod() {
        Assertions.assertThrows(AuthorizationDeniedException.class, () -> externalEmployeeService.getListOfTransactions(0, 10, ApiTransactionStatus.APPROVED));
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE" })
    void shouldReturnResponse_WhenEmployeeRunsSecuredMethod() {
        EmployeeTransactionsResponse employeeTransactionsResponse = externalEmployeeService.getListOfTransactions(0, 10, ApiTransactionStatus.APPROVED);
        Assertions.assertNotNull(employeeTransactionsResponse);
    }

    @Test
    void test_EmployeeEndpoints_ShouldOnlyBeAccessibleByEmployeeRole() {
        // customer registration
        var customerRegisterRequest = new RegisterWithEmailPasswordRequest()
                .email("customer@gmail.com")
                .password("1234")
                .name("test")
                .surname("surname")
                .tckn(VALID_TCKN)
                ;

        testRestTemplate.postForEntity(generateUri("/api/auth/customer/create"), customerRegisterRequest, RegisterApiResponse.class);

        var employeeRegisterRequest = new RegisterWithEmailPasswordRequest()
                .email("employee@gmail.com")
                .password("1234")
                .name("test")
                .surname("surname")
                .tckn(VALID_TCKN)
                ;
        testRestTemplate.postForEntity(generateUri("/api/auth/employee/create"), employeeRegisterRequest, RegisterApiResponse.class);

        // login with users
        var customerLoginRequest = new LoginRequest().email(customerRegisterRequest.getEmail())
                .password(customerRegisterRequest.getPassword());
        var customerLoginApiResponseEntity = testRestTemplate.postForEntity(generateUri("/api/auth/login"), customerLoginRequest, LoginApiResponse.class);
        LoginApiResponse customerLoginApiResponse = customerLoginApiResponseEntity.getBody();
        Assertions.assertNotNull(customerLoginApiResponse);
        LoginResponse customerLoginResponse = customerLoginApiResponse.getResponse();
        Assertions.assertAll(
                () -> Assertions.assertTrue(customerLoginApiResponse.getIsSuccess()),
                () -> Assertions.assertEquals("customer@gmail.com", customerLoginResponse.getEmail()),
                () -> Assertions.assertNotNull(customerLoginResponse.getToken())
        );

        var employeeLoginRequest = new LoginRequest().email(employeeRegisterRequest.getEmail())
                .password(employeeRegisterRequest.getPassword());
        var employeeLoginApiResponseEntity = testRestTemplate.postForEntity(generateUri("/api/auth/login"), employeeLoginRequest, LoginApiResponse.class);
        LoginApiResponse employeeLoginApiResponse = employeeLoginApiResponseEntity.getBody();
        Assertions.assertNotNull(employeeLoginApiResponse);

        LoginResponse employeeLoginResponse = employeeLoginApiResponse.getResponse();
        Assertions.assertAll(
                () -> Assertions.assertTrue(employeeLoginApiResponse.getIsSuccess()),
                () -> Assertions.assertEquals("employee@gmail.com", employeeLoginResponse.getEmail()),
                () -> Assertions.assertNotNull(employeeLoginResponse.getToken())
        );

        // customer should not access employee endpoints
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + customerLoginResponse.getToken());

        ResponseEntity<GenericApiResponse> accessDeniedResponse = testRestTemplate.exchange(
                generateUri("/api/employee/transactions"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GenericApiResponse.class
        );
        Assertions.assertNotNull(accessDeniedResponse.getBody());
        GenericApiResponse<?> accessDeniedApiResponse = accessDeniedResponse.getBody();
        Assertions.assertAll(
                () -> Assertions.assertFalse(accessDeniedApiResponse.isSuccess),
                () -> Assertions.assertEquals(ApiErrorInfo.ACCESS_DENIED_EXCEPTION.getMessage(), accessDeniedApiResponse.errorResponses.getFirst().getTitle())
        );

        headers.set("Authorization", "Bearer " + employeeLoginResponse.getToken());
        var allTransactions = testRestTemplate.exchange(
                generateUri("/api/employee/transactions"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                EmployeeTransactionsApiResponse.class
        );
        Assertions.assertNotNull(allTransactions.getBody());
        EmployeeTransactionsApiResponse transactionResponse = allTransactions.getBody();
        Assertions.assertAll(
                () -> Assertions.assertTrue(transactionResponse.getIsSuccess()),
                () -> Assertions.assertTrue(transactionResponse.getResponse().getData().isEmpty())
        );
    }
}
