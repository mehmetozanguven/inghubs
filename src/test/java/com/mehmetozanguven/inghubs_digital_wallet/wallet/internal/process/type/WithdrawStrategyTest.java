package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type;

import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

class WithdrawStrategyTest {

    TransactionTypeStrategy withdrawStrategy;

    @BeforeEach
    void setup() {
        withdrawStrategy = new WithdrawStrategy();
    }

    @Test
    void doAction_ShouldFailTransaction_WhenWalletNotFound() {
        Transaction transaction = Transaction.builder().wallet(null).build();
        var txContext = new ProcessTransactionContext(transaction);
        OperationResult<ProcessTransactionContext> result = withdrawStrategy.doAction(txContext);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result.getReturnedValue()),
                () -> Assertions.assertEquals(TransactionStatus.FAIL, result.getReturnedValue().givenTransaction().getTransactionStatus())
        );
    }

    @ParameterizedTest
    @MethodSource("walletNotOpenForWithdraw")
    void doAction_ShouldFailTransaction_WhenWalletNotOpenForWithdraw(Transaction transaction) {
        var txContext = new ProcessTransactionContext(transaction);
        OperationResult<ProcessTransactionContext> result = withdrawStrategy.doAction(txContext);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result.getReturnedValue()),
                () -> Assertions.assertEquals(TransactionStatus.FAIL, result.getReturnedValue().givenTransaction().getTransactionStatus()),
                () -> Assertions.assertEquals(ApiErrorInfo.WALLET_NOT_OPEN_FOR_WITHDRAW.getCode(), result.getReturnedValue().givenTransaction().getTransactionInfo().getCode())
        );
    }

    @ParameterizedTest
    @MethodSource("walletHasNotEnoughBalance")
    void doAction_ShouldFailTransaction_WhenNotEnoughAmount(Transaction transaction) {
        var txContext = new ProcessTransactionContext(transaction);
        OperationResult<ProcessTransactionContext> result = withdrawStrategy.doAction(txContext);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result.getReturnedValue()),
                () -> Assertions.assertEquals(TransactionStatus.FAIL, result.getReturnedValue().givenTransaction().getTransactionStatus()),
                () -> Assertions.assertEquals(ApiErrorInfo.TRANSACTION_OPERATION_FAILED.getCode(), result.getReturnedValue().givenTransaction().getTransactionInfo().getCode())
        );
    }

    @ParameterizedTest
    @MethodSource("givenTransactions")
    void doAction_ShouldAddTxAmountToBalanceAndUsableBalance_AccordingToTransactionStatus(Transaction givenTransaction, TransactionStatus expectedStatus,
                                                                                          FinancialMoney expectedBalance, FinancialMoney expectedUsableBalance,
                                                                                          boolean isProcessed) {
        var txContext = new ProcessTransactionContext(givenTransaction);
        OperationResult<ProcessTransactionContext> result = withdrawStrategy.doAction(txContext);
        Assertions.assertNotNull(result.getReturnedValue());
        Transaction updatedTx = result.getReturnedValue().givenTransaction();
        if (isProcessed) {
            Assertions.assertNotNull(updatedTx.getProcessedAt());
        }
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedStatus, updatedTx.getTransactionStatus()),
                () -> Assertions.assertEquals(expectedBalance, updatedTx.getWallet().getBalance(), "Compare balance"),
                () -> Assertions.assertEquals(expectedUsableBalance, updatedTx.getWallet().getUsableBalance(), "Compare usableBalance")
        );
    }

    static Stream<Arguments> walletNotOpenForWithdraw() {
        return Stream.of(
                Arguments.of(
                        Transaction.builder()
                                .wallet(Wallet.builder().activeForShopping(null).build())
                                .build()
                ),
                Arguments.of(
                        Transaction.builder()
                                .wallet(Wallet.builder().activeForShopping(false).build())
                                .build()
                )
        );
    }

    static Stream<Arguments> walletHasNotEnoughBalance() {
        return Stream.of(
                Arguments.of(
                        Transaction.builder()
                                .amount(BigDecimal.valueOf(100L))
                                .transactionType(TransactionType.WITHDRAW)
                                .wallet(Wallet.builder()
                                        .activeForWithdraw(true)
                                        .balance(BigDecimal.valueOf(50L))
                                        .usableBalance(BigDecimal.valueOf(300L))
                                        .build()
                                )
                                .build()
                ),
                Arguments.of(
                        Transaction.builder()
                                .amount(BigDecimal.valueOf(100L))
                                .transactionType(TransactionType.WITHDRAW)
                                .wallet(Wallet.builder()
                                        .activeForWithdraw(true)
                                        .balance(BigDecimal.valueOf(300L))
                                        .usableBalance(BigDecimal.valueOf(50L))
                                        .build()
                                )
                                .build()
                )
        );

    }

    static Stream<Arguments> givenTransactions() {
        FinancialMoney walletUsableBalance = new FinancialMoney(BigDecimal.valueOf(200L), CurrencyType.TRY);
        FinancialMoney walletBalance = new FinancialMoney(BigDecimal.valueOf(100L), CurrencyType.TRY);
        CurrencyType walletCurrencyType = CurrencyType.TRY;

        Wallet.WalletBuilder txWallet = Wallet.builder()
                .balance(walletBalance.amount())
                .activeForWithdraw(true)
                .usableBalance(walletUsableBalance.amount())
                .currencyType(walletCurrencyType);

        return Stream.of(
                Arguments.of(
                        Transaction.builder()
                                .amount(new FinancialMoney(BigDecimal.valueOf(30L), CurrencyType.TRY).amount())
                                .transactionType(TransactionType.WITHDRAW)
                                .wallet(txWallet.build())
                                .transactionStatus(TransactionStatus.APPROVED)
                                .currencyType(walletCurrencyType)
                                .build(),
                        TransactionStatus.APPROVED,
                        FinancialMoney.subtractMoney(walletBalance, new FinancialMoney(BigDecimal.valueOf(30L), CurrencyType.TRY)),
                        FinancialMoney.subtractMoney(walletUsableBalance, new FinancialMoney(BigDecimal.valueOf(30L), CurrencyType.TRY)),
                        true
                ),
                Arguments.of(
                        Transaction.builder()
                                .amount(new FinancialMoney(BigDecimal.valueOf(40L), CurrencyType.TRY).amount())
                                .transactionType(TransactionType.WITHDRAW)
                                .wallet(txWallet.build())
                                .transactionStatus(TransactionStatus.PENDING)
                                .currencyType(walletCurrencyType)
                                .build(),
                        TransactionStatus.PENDING,
                        FinancialMoney.subtractMoney(walletBalance, new FinancialMoney(BigDecimal.valueOf(40L), CurrencyType.TRY)),
                        FinancialMoney.subtractMoney(walletUsableBalance, new FinancialMoney(BigDecimal.ZERO, CurrencyType.TRY)),
                        false
                ),
                Arguments.of(
                        Transaction.builder()
                                .amount(new FinancialMoney(BigDecimal.valueOf(35L), CurrencyType.TRY).amount())
                                .transactionType(TransactionType.WITHDRAW)
                                .wallet(txWallet.build())
                                .transactionStatus(TransactionStatus.APPROVED_FROM_PENDING)
                                .currencyType(walletCurrencyType)
                                .build(),
                        TransactionStatus.APPROVED,
                        FinancialMoney.subtractMoney(walletBalance, new FinancialMoney(BigDecimal.ZERO, CurrencyType.TRY)),
                        FinancialMoney.subtractMoney(walletUsableBalance, new FinancialMoney(BigDecimal.valueOf(35L), CurrencyType.TRY)),
                        true
                )
        );
    }
}