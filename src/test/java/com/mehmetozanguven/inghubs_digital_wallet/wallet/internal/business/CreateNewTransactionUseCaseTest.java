package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.business;

import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.opposite_party_type.OppositePartyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiException;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.TransactionModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapperImpl;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNewTransactionUseCaseTest {
    @Mock
    WalletRepository walletRepository;
    @Mock
    TransactionRepository transactionRepository;
    WalletMapper walletMapper = new WalletMapperImpl();

    CreateNewTransactionUseCase createNewTransactionUseCase;

    @BeforeEach
    void setup() {
        createNewTransactionUseCase = new CreateNewTransactionUseCase(
                walletRepository,
                transactionRepository,
                walletMapper
        );
    }

    @Test
    void applyBusiness_ShouldThrowException_WhenWalletNotFound() {
        when(walletRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        CreateNewTransactionUseCase.CreateTransactionRequest txRequest = CreateNewTransactionUseCase.CreateTransactionRequest.builder()
                .build();

        OperationResult<TransactionModel> result =  createNewTransactionUseCase.applyBusiness(txRequest);
        Assertions.assertAll(
                () -> Assertions.assertFalse(result.isValid()),
                () -> Assertions.assertNull(result.getReturnedValue()),
                () -> Assertions.assertEquals(ApiErrorInfo.TRANSACTION_OPERATION_FAILED.getCode(), result.getException().getFirst().getCode())
        );
    }

    @Test
    void applyBusiness_ShouldThrowException_WhenTransactionAmountIsLessThanZero() {
        when(walletRepository.findOne(any(Specification.class))).thenReturn(Optional.of(
                Wallet.builder()
                        .currencyType(CurrencyType.EURO)
                        .build()
        ));

        CreateNewTransactionUseCase.CreateTransactionRequest txRequest = CreateNewTransactionUseCase.CreateTransactionRequest.builder()
                .financialMoney(new FinancialMoney(BigDecimal.valueOf(-10L), CurrencyType.TRY))
                .build();

        OperationResult<TransactionModel> result =  createNewTransactionUseCase.applyBusiness(txRequest);
        Assertions.assertAll(
                () -> Assertions.assertFalse(result.isValid()),
                () -> Assertions.assertNull(result.getReturnedValue()),
                () -> Assertions.assertEquals(ApiErrorInfo.TRANSACTION_OPERATION_FAILED.getCode(), result.getException().getFirst().getCode())
        );
    }

    @Test
    void applyBusiness_ShouldThrowException_WhenWalletCurrencyNotMatchedWithTransactionCurrency() {
        when(walletRepository.findOne(any(Specification.class))).thenReturn(Optional.of(
                Wallet.builder()
                        .currencyType(CurrencyType.EURO)
                        .build()
        ));

        CreateNewTransactionUseCase.CreateTransactionRequest txRequest = CreateNewTransactionUseCase.CreateTransactionRequest.builder()
                .financialMoney(new FinancialMoney(BigDecimal.valueOf(10L), CurrencyType.TRY))
                .build();

        OperationResult<TransactionModel> result =  createNewTransactionUseCase.applyBusiness(txRequest);
        Assertions.assertAll(
                () -> Assertions.assertFalse(result.isValid()),
                () -> Assertions.assertNull(result.getReturnedValue()),
                () -> Assertions.assertEquals(ApiErrorInfo.TRANSACTION_OPERATION_FAILED.getCode(), result.getException().getFirst().getCode())
        );
    }

    @Test
    void applyBusiness_ShouldSaveRepository_WhenRequestValid() {
        when(walletRepository.findOne(any(Specification.class))).thenReturn(Optional.of(
                Wallet.builder()
                        .customerId("test")
                        .activeForWithdraw(true)
                        .currencyType(CurrencyType.TRY)
                        .build()
        ));

        CreateNewTransactionUseCase.CreateTransactionRequest txRequest = CreateNewTransactionUseCase.CreateTransactionRequest.builder()
                .transactionType(TransactionType.DEPOSIT)
                .oppositePartyType(OppositePartyType.PAYMENT)
                .financialMoney(new FinancialMoney(BigDecimal.valueOf(10L), CurrencyType.TRY))
                .build();
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(transactionRepository).save(any());
        OperationResult<TransactionModel> result =  createNewTransactionUseCase.applyBusiness(txRequest);

        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isValid()),
                () -> Assertions.assertNotNull(result.getReturnedValue()),
                () -> Assertions.assertEquals(OppositePartyType.PAYMENT, result.getReturnedValue().getOppositePartyType())
        );
    }
}