package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.api;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppSecureUser;
import com.mehmetozanguven.inghubs_digital_wallet.security.AuthenticatedUser;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.WalletInternalService;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.api.controller.WalletControllerApi;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletControllerApi {
    private final WalletInternalService walletInternalService;
    private final WalletMapper walletMapper;

    @Override
    public WalletTransactionsApiResponse doGetTransactions(String walletId, Integer page, Integer size, ApiTransactionStatus transactionStatus) {
        AppSecureUser loggedInUser = AuthenticatedUser.getLoggedInUser();
        PageRequest pageRequest = PageRequest.of(page, size);
        WalletTransactionsResponse walletTransactionsResponse = walletInternalService.getListOfTransactionsInWallet(loggedInUser.getUserId(), walletId, pageRequest, transactionStatus);
        return new WalletTransactionsApiResponse()
                .isSuccess(true)
                .httpStatusCode(HttpStatus.OK.value())
                .response(walletTransactionsResponse)
                ;
    }

    @Override
    public WalletApiResponses doGetWallets(ApiCurrencyCode currency, BigDecimal balanceAmountEqualOrHigherThan) {
        AppSecureUser loggedInUser = AuthenticatedUser.getLoggedInUser();
        List<WalletModel> customerWallets = walletInternalService.getCustomerWallets(loggedInUser.getUserId(), currency, balanceAmountEqualOrHigherThan);
        List<WalletResponse> walletResponses = walletMapper.createApiResponsesFromWalletModels(customerWallets);

        return new WalletApiResponses()
                .httpStatusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .response(walletResponses)
                ;
    }

    @Override
    public WalletApiResponse doCreateWallet(WalletCreateApiRequest walletRequest) {
        AppSecureUser loggedInUser = AuthenticatedUser.getLoggedInUser();
        WalletResponse walletResponse = walletInternalService.createWalletForCustomer(walletRequest, loggedInUser.getUserId());

        return new WalletApiResponse()
                .httpStatusCode(HttpStatus.CREATED.value())
                .isSuccess(true)
                .response(walletResponse)
                ;
    }

    @Override
    public TransactionApiResponse doCreateDepositTransaction(DepositApiRequest depositApiRequest) {
        AppSecureUser loggedInUser = AuthenticatedUser.getLoggedInUser();
        TransactionResponse transactionResponse = walletInternalService.createDepositTransaction(depositApiRequest, loggedInUser.getUserId());
        return new TransactionApiResponse()
                .httpStatusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .response(transactionResponse)
                ;
    }

    @Override
    public TransactionApiResponse doCreateWithdrawTransaction(WithdrawApiRequest withdrawApiRequest) {
        AppSecureUser loggedInUser = AuthenticatedUser.getLoggedInUser();
        TransactionResponse transactionResponse = walletInternalService.createWithdrawTransaction(withdrawApiRequest, loggedInUser.getUserId());
        return new TransactionApiResponse()
                .httpStatusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .response(transactionResponse)
                ;
    }
}
