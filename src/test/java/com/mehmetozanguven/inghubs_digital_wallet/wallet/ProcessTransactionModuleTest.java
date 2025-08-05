package com.mehmetozanguven.inghubs_digital_wallet.wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.BaseApplicationModuleTest;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.WalletInternalService;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionService;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.TransactionPublisher;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;

import java.math.BigDecimal;
import java.time.Duration;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, extraIncludes = "core")
public class ProcessTransactionModuleTest extends BaseApplicationModuleTest {
    @Autowired
    private ProcessTransactionService processTransactionService;
    @Autowired
    private WalletInternalService walletInternalService;
    @Autowired
    private WalletExternalEmployeeService walletExternalEmployeeService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionPublisher transactionPublisher;

    private WalletResponse createWalletForCustomer(String customerId, FinancialMoney initialBalanceRequest) {
        WalletResponse walletResponse = walletInternalService.createWalletForCustomer(
                new WalletCreateApiRequest()
                        .walletName("test")
                        .activeForShopping(true)
                        .activeForWithdraw(true)
                        .currencyType(ApiCurrencyCode.TRY)
                ,
                customerId
        );

        Assertions.assertNotNull(walletResponse);

        return walletResponse;
    }


    @Test
    void test_WalletDepositWithBiggerAmount() {
        String customerId = "test";
        // Create a wallet
        FinancialMoney initialBalanceRequest = FinancialMoney.builder().amount(BigDecimal.ZERO).currency(CurrencyType.TRY).build();
        WalletResponse createdWallet = createWalletForCustomer(customerId, initialBalanceRequest);

        // Make deposit with given amount higher than 1000
        TransactionResponse transactionResponse = walletInternalService.createDepositTransaction(
                new DepositApiRequest()
                        .walletId(createdWallet.getId())
                        .sourceOfDeposit(ApiPartyType.IBAN)
                        .oppositeParty("IBAN")
                        .money(new ApiMoney(BigDecimal.valueOf(5000L), ApiCurrencyCode.TRY)),
                customerId
        );

        FinancialMoney expectedWalletAmount = new FinancialMoney(BigDecimal.valueOf(5000L), CurrencyType.TRY);

        // process transaction
        transactionPublisher.pushProcessingTransactions();
        waitNMillis(1500);
        // two or more consecutive tries should not affect the result
        transactionPublisher.pushPendingTransactions();
        waitNMillis(1500);
        transactionPublisher.pushPendingTransactions();
        waitNMillis(1500);

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction should be pending and only wallet.balance must be updated
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.PENDING);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(1, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(initialBalanceRequest, walletModel.getUsableBalance()))
                    );
                });

        // someone approved transaction
        boolean approvalResult = walletExternalEmployeeService.approveTransaction(transactionResponse.getWalletId(), transactionResponse.getTransactionId());
        Assertions.assertTrue(approvalResult);


        transactionPublisher.pushApprovedFromPendingTransactions();
        waitNMillis(1500);

        // wait to process transaction through kafka
        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction should be approved and wallet.balance & wallet.usableBalance must be updated
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(1, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
                    );
                });

        // another schedulerJob should not affect the result
        transactionPublisher.pushApprovedTransactions();
        waitNMillis(1500);

        WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
        WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, updatedTransactions.getTotalElements()),
                () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
        );
    }



    @Test
    void test_WalletDepositFlow() {
        FinancialMoney ZERO_BALANCE = FinancialMoney.builder().amount(BigDecimal.ZERO).currency(CurrencyType.TRY).build();

        String customerId = "test";

        WalletResponse createdWallet = createWalletForCustomer(customerId, ZERO_BALANCE);

        // Make deposit with given amount lower than 1000
        TransactionResponse transactionResponse = walletInternalService.createDepositTransaction(
                new DepositApiRequest()
                        .walletId(createdWallet.getId())
                        .sourceOfDeposit(ApiPartyType.IBAN)
                        .oppositeParty("IBAN")
                        .money(new ApiMoney(BigDecimal.valueOf(10L), ApiCurrencyCode.TRY)),
                customerId
        );
        Assertions.assertNotNull(transactionResponse.getTransactionId());
        Assertions.assertEquals(TransactionStatus.PROCESSING.getValue(), transactionResponse.getTransactionStatus().getValue());

        WalletTransactionsResponse walletTransactionsResponse = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), null);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, walletTransactionsResponse.getTotalElements()),
                () -> Assertions.assertEquals(0, walletTransactionsResponse.getPage()),
                () -> Assertions.assertEquals(10, walletTransactionsResponse.getSize()),
                () -> Assertions.assertEquals(transactionResponse.getTransactionId(), walletTransactionsResponse.getData().getFirst().getTransactionId())
        );
        FinancialMoney expectedWalletAmount = new FinancialMoney(BigDecimal.valueOf(10L), CurrencyType.TRY);

        // process, TransactionStatus.PROCESSING
        transactionPublisher.pushProcessingTransactions();

        // wait to process transaction through kafka
        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction status should be APPROVED, but waiting for processing balance operations
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(1, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertFalse(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertFalse(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
                    );
                });

        // process, TransactionStatus.APPROVED
        transactionPublisher.pushApprovedTransactions();
        // wait to process transaction through kafka
        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction status should be APPROVED,
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(1, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
                    );
                });

        // Make another deposit with given amount lower than 1000
        walletInternalService.createDepositTransaction(
                new DepositApiRequest()
                        .walletId(createdWallet.getId())
                        .sourceOfDeposit(ApiPartyType.IBAN)
                        .oppositeParty("IBAN")
                        .money(new ApiMoney(BigDecimal.valueOf(10L), ApiCurrencyCode.TRY)),
                customerId
        );
        transactionPublisher.pushProcessingTransactions();
        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(2, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
                    );
                });
        transactionPublisher.pushApprovedTransactions();

        FinancialMoney nextWallet = new FinancialMoney(BigDecimal.valueOf(20L), CurrencyType.TRY);

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction should be approved and wallet must be updated
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(2, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(nextWallet, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(nextWallet, walletModel.getUsableBalance()))
                    );
                });
    }

    @Test
    void test_WalletWithdrawWithBiggerAmount() {
        String customerId = "test";
        // Create a wallet
        FinancialMoney initialBalanceRequest = FinancialMoney.builder().amount(BigDecimal.ZERO).currency(CurrencyType.TRY).build();
        WalletResponse createdWallet = createWalletForCustomer(customerId, initialBalanceRequest);

        // Make deposit with amount higher than 1000
        TransactionResponse depositTransaction = walletInternalService.createDepositTransaction(
                new DepositApiRequest()
                        .walletId(createdWallet.getId())
                        .sourceOfDeposit(ApiPartyType.IBAN)
                        .oppositeParty("IBAN")
                        .money(new ApiMoney(BigDecimal.valueOf(10000L), ApiCurrencyCode.TRY)),
                customerId
        );

        // process tx.PROCESSING -> tx.PENDING
        transactionPublisher.pushProcessingTransactions();
        waitNMillis(1500);
        // process tx.PENDING
        transactionPublisher.pushPendingTransactions();
        waitNMillis(1500);

        // update tx to tx.APPROVED_FROM_PENDING
        walletExternalEmployeeService.approveTransaction(depositTransaction.getWalletId(), depositTransaction.getTransactionId());
        // process tx.APPROVED_FROM_PENDING -> tx.APPROVE
        transactionPublisher.pushApprovedFromPendingTransactions();
        waitNMillis(1500);
        transactionPublisher.pushApprovedTransactions();
        waitNMillis(1500);

        FinancialMoney nextBalanceRequestAfterDeposit = initialBalanceRequest.withAmount(BigDecimal.valueOf(10000L));

        // Make withdraw with given amount higher than 1000
        TransactionResponse transactionResponse = walletInternalService.createWithdrawTransaction(
                new WithdrawApiRequest()
                        .walletId(createdWallet.getId())
                        .sourceOfDeposit(ApiPartyType.IBAN)
                        .oppositeParty("IBAN")
                        .money(new ApiMoney(BigDecimal.valueOf(2000L), ApiCurrencyCode.TRY)),
                customerId
        );

        FinancialMoney expectedWalletAmount = new FinancialMoney(BigDecimal.valueOf(8000L), CurrencyType.TRY);

        // process tx.PROCESSING -> tx.PENDING
        transactionPublisher.pushProcessingTransactions();
        waitNMillis(1500);
        // process tx.PENDING
        // two or more consecutive tries should not affect the result
        transactionPublisher.pushPendingTransactions();
        waitNMillis(1500);
        transactionPublisher.pushPendingTransactions();
        waitNMillis(1500);

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction should be pending and only wallet.balance must be updated
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.PENDING);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(1, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(nextBalanceRequestAfterDeposit, walletModel.getUsableBalance()))
                    );
                });

        // update tx to tx.APPROVED_FROM_PENDING
        boolean approvalResult = walletExternalEmployeeService.approveTransaction(transactionResponse.getWalletId(), transactionResponse.getTransactionId());
        Assertions.assertTrue(approvalResult);

        // process tx.APPROVED_FROM_PENDING -> tx.APPROVE
        transactionPublisher.pushApprovedFromPendingTransactions();
        waitNMillis(1500);

        // wait to process transaction through kafka
        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction should be approved and wallet.balance & wallet.usableBalance must be updated
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(2, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
                    );
                });

        // another schedulerJob should not affect the result
        transactionPublisher.pushApprovedTransactions();
        waitNMillis(1500);

        WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
        WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, updatedTransactions.getTotalElements()),
                () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
        );
    }


    @Test
    void test_WalletWithdrawFlow() {
        String customerId = "test";
        // Create a wallet
        FinancialMoney initialBalanceRequest = FinancialMoney.builder().amount(BigDecimal.ZERO).currency(CurrencyType.TRY).build();
        WalletResponse createdWallet = createWalletForCustomer(customerId, initialBalanceRequest);

        // Make deposit
        walletInternalService.createDepositTransaction(
                new DepositApiRequest()
                        .walletId(createdWallet.getId())
                        .sourceOfDeposit(ApiPartyType.IBAN)
                        .oppositeParty("IBAN")
                        .money(new ApiMoney(BigDecimal.valueOf(250L), ApiCurrencyCode.TRY)),
                customerId
        );

        transactionPublisher.pushProcessingTransactions();
        waitNMillis(1500);
        transactionPublisher.pushPendingTransactions();
        waitNMillis(1500);

        // Make withdraw
        TransactionResponse transactionResponse = walletInternalService.createWithdrawTransaction(
                new WithdrawApiRequest()
                        .walletId(createdWallet.getId())
                        .sourceOfDeposit(ApiPartyType.IBAN)
                        .oppositeParty("IBAN")
                        .money(new ApiMoney(BigDecimal.valueOf(50L), ApiCurrencyCode.TRY)),
                customerId
        );

        FinancialMoney expectedWalletAmount = new FinancialMoney(BigDecimal.valueOf(200L), CurrencyType.TRY);

        transactionPublisher.pushProcessingTransactions();
        waitNMillis(1500);
        transactionPublisher.pushApprovedTransactions();
        waitNMillis(1500);

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    // transaction status should be APPROVED,
                    WalletTransactionsResponse updatedTransactions = walletInternalService.getListOfTransactionsInWallet(customerId, transactionResponse.getWalletId(), PageRequest.of(0, 10), ApiTransactionStatus.APPROVED);
                    WalletModel walletModel = walletInternalService.getCustomerWallet(customerId, transactionResponse.getWalletId());

                    Assertions.assertAll(
                            () -> Assertions.assertEquals(2, updatedTransactions.getTotalElements()),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getBalance())),
                            () -> Assertions.assertTrue(FinancialMoney.isEqualAmount(expectedWalletAmount, walletModel.getUsableBalance()))
                    );
                });
    }
}
