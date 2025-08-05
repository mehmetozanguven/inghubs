package com.mehmetozanguven.inghubs_digital_wallet.employee;

import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.api.controller.EmployeeControllerApi;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmployeeController implements EmployeeControllerApi {
    private final EmployeeInternalService employeeInternalService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterApiResponse doRegisterEmployeeWithEmailPassword(RegisterWithEmailPasswordRequest registerWithEmailPasswordRequest) {
        registerWithEmailPasswordRequest.password((passwordEncoder.encode(registerWithEmailPasswordRequest.getPassword())));
        RegisterResponse registerResponse = employeeInternalService.createEmployee(registerWithEmailPasswordRequest);

        return new RegisterApiResponse()
                .isSuccess(true)
                .httpStatusCode(HttpStatus.CREATED.value())
                .response(registerResponse)
                ;
    }


    @Override
    public Boolean doApproveTransaction(ApproveTransactionApiRequest approveTransactionApiRequest) {
        return employeeInternalService.approveTransaction(approveTransactionApiRequest);
    }

    @Override
    public EmployeeTransactionsApiResponse doGetAllTransaction(Integer page, Integer size, ApiTransactionStatus transactionStatus) {
        EmployeeTransactionsResponse response = employeeInternalService.getListOfTransactions(page, size, transactionStatus);
        return new EmployeeTransactionsApiResponse()
                .isSuccess(true)
                .httpStatusCode(HttpStatus.OK.value())
                .response(response)
                ;
    }
}
