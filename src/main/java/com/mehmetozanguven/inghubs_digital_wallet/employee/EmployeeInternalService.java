package com.mehmetozanguven.inghubs_digital_wallet.employee;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerExternalService;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletExternalEmployeeService;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeInternalService {
    private final CustomerExternalService customerExternalService;
    private final WalletExternalEmployeeService walletExternalEmployeeService;

    public RegisterResponse createEmployee(RegisterWithEmailPasswordRequest registerWithEmailPasswordRequest){
        String createdMail = customerExternalService.createEmployee(registerWithEmailPasswordRequest);
        return new RegisterResponse()
                .email(createdMail);
    }

    public EmployeeTransactionsResponse getListOfTransactions(Integer page, Integer size, ApiTransactionStatus apiTransactionStatus) {
        return walletExternalEmployeeService.getListOfTransactions(page, size, apiTransactionStatus);
    }


    public boolean approveTransaction(ApproveTransactionApiRequest approveTransactionApiRequest) {
        return walletExternalEmployeeService.approveTransaction(approveTransactionApiRequest.getWalletId(), approveTransactionApiRequest.getTransactionId());
    }
}
