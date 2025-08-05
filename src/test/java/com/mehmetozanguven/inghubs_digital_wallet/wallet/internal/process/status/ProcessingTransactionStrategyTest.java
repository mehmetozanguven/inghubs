package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;

import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.properties.TransactionProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingTransactionStrategyTest {

    @Mock
    TransactionProperties transactionProperties;

    TransactionStatusStrategy transactionStatusStrategy;

    @BeforeEach
    void setup() {
        transactionStatusStrategy = new ProcessingTransactionStrategy(transactionProperties);
    }

    @ParameterizedTest
    @MethodSource("givenTransactions")
    void doAction_ShouldUpdateTransactionStatus_AccordingToMaxAllowedAmount(Long maxAllowedAmount,
                                                                            Transaction givenTransaction,
                                                                            TransactionStatus expectedStatus,
                                                                            FinancialMoney expectedBalance, FinancialMoney expectedUsableBalance) {
        when(transactionProperties.getMaxAllowedAmountPerTransaction()).thenReturn(new FinancialMoney(BigDecimal.valueOf(maxAllowedAmount), CurrencyType.UNKNOWN));
        var txContext = new ProcessTransactionContext(givenTransaction);
        OperationResult<ProcessTransactionContext> result = transactionStatusStrategy.doAction(txContext);
        Assertions.assertNotNull(result.getReturnedValue());
        Transaction updatedTx = result.getReturnedValue().givenTransaction();
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedStatus, updatedTx.getTransactionStatus()),
                () -> Assertions.assertEquals(expectedBalance, updatedTx.getWallet().getBalance(), "Compare balance"),
                () -> Assertions.assertEquals(expectedUsableBalance, updatedTx.getWallet().getUsableBalance(), "Compare usableBalance")
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
                        1000L,
                        Transaction.builder()
                                .amount(new FinancialMoney(BigDecimal.valueOf(1001L), CurrencyType.TRY).amount())
                                .transactionType(TransactionType.WITHDRAW)
                                .wallet(txWallet.build())
                                .transactionStatus(TransactionStatus.APPROVED)
                                .currencyType(walletCurrencyType)
                                .build(),
                        TransactionStatus.PENDING,
                        walletBalance,
                        walletUsableBalance
                ),
                Arguments.of(
                        2000L,
                        Transaction.builder()
                                .amount(new FinancialMoney(BigDecimal.valueOf(2000L), CurrencyType.TRY).amount())
                                .transactionType(TransactionType.WITHDRAW)
                                .wallet(txWallet.build())
                                .transactionStatus(TransactionStatus.APPROVED)
                                .currencyType(walletCurrencyType)
                                .build(),
                        TransactionStatus.APPROVED,
                        walletBalance,
                        walletUsableBalance
                )
        );
    }
}