package com.mehmetozanguven.inghubs_digital_wallet.customer.internal.api;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerRoleModel;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.CustomerInternalService;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.api.controller.CustomerControllerApi;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterApiResponse;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterResponse;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterWithEmailPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerControllerApi {
    private final CustomerInternalService customerInternalService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterApiResponse doRegisterWithEmailPassword(RegisterWithEmailPasswordRequest registerWithEmailPasswordRequest) {
        registerWithEmailPasswordRequest.password((passwordEncoder.encode(registerWithEmailPasswordRequest.getPassword())));

        String email = customerInternalService.createCustomer(registerWithEmailPasswordRequest, Set.of(CustomerRoleModel.builder().appRole(AppRole.CUSTOMER).build()));
        RegisterResponse registerResponse = new RegisterResponse()
                .email(email);
        return new RegisterApiResponse()
                .isSuccess(true)
                .httpStatusCode(HttpStatus.CREATED.value())
                .response(registerResponse)
                ;
    }
}
