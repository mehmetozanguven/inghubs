package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.business;

import com.mehmetozanguven.inghubs_digital_wallet.core.ApiApplyOperationResultLogic;
import com.mehmetozanguven.inghubs_digital_wallet.core.BusinessUseCase;
import com.mehmetozanguven.inghubs_digital_wallet.core.DateOperation;
import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.opposite_party_type.OppositePartyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.TransactionModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.*;
import jakarta.persistence.criteria.Predicate;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@BusinessUseCase
@RequiredArgsConstructor
public class CreateNewTransactionUseCase implements ApiApplyOperationResultLogic<CreateNewTransactionUseCase.CreateTransactionRequest, CreateNewTransactionUseCase.CreateTransactionInnerRequest, TransactionModel> {
    @Builder
    public record CreateTransactionRequest(
                                            String customerId,
                                            String walletId,
                                            TransactionType transactionType,
                                            FinancialMoney financialMoney,
                                            TransactionInfo transactionInfo,
                                            OppositePartyType oppositePartyType,
                                            String oppositeParty){}
    @Builder
    public record CreateTransactionInnerRequest(Transaction newTransaction) {};

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;


    @Override
    public OperationResult<CreateTransactionInnerRequest> logicBefore(CreateTransactionRequest request) {
        Optional<Wallet> walletInDb = walletRepository.findOne((Specification<Wallet>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    criteriaBuilder.equal(root.get(Wallet_.customerId), request.customerId())
            );
            predicates.add(
                    criteriaBuilder.equal(root.get(Wallet_.id), request.walletId())
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
        if (walletInDb.isEmpty()) {
            return OperationResult.<CreateTransactionInnerRequest>builder()
                    .addException(ApiErrorInfo.TRANSACTION_OPERATION_FAILED, "Wallet not found")
                    .build();
        }

        if (FinancialMoney.isLessThanGivenAmount(request.financialMoney(), request.financialMoney().withAmount(BigDecimal.ZERO))) {
            return OperationResult.<CreateTransactionInnerRequest>builder()
                    .addException(ApiErrorInfo.TRANSACTION_OPERATION_FAILED, "Transaction amount can not be lower than zero")
                    .build();
        }

        Wallet foundWallet = walletInDb.get();
        if (!foundWallet.getCurrencyType().equals(request.financialMoney().currency())) {
            return OperationResult.<CreateTransactionInnerRequest>builder()
                    .addException(ApiErrorInfo.TRANSACTION_OPERATION_FAILED, "Wallet doesn't support for the requested currency")
                    .build();
        }

        Transaction.TransactionBuilder newTransaction = Transaction.builder()
                .transactionType(request.transactionType())
                .transactionStatus(TransactionStatus.PROCESSING)
                .wallet(foundWallet)
                .currencyType(foundWallet.getCurrencyType())
                .amount(request.financialMoney().amount())
                .expirationTime(DateOperation.addDurationToUTCNow(Duration.ofMinutes(5)))
                .transactionInfo(request.transactionInfo())
                .oppositePartyType(request.oppositePartyType())
                .oppositeParty(request.oppositeParty())
                ;

        return OperationResult.<CreateTransactionInnerRequest>builder()
                .addReturnedValue(CreateTransactionInnerRequest.builder()
                        .newTransaction(newTransaction.build())
                        .build()
                )
                .build();
    }

    @Override
    public OperationResult<TransactionModel> executeLogic(CreateTransactionInnerRequest createTransactionInnerRequest) {
        Transaction savedTransaction = transactionRepository.save(createTransactionInnerRequest.newTransaction());
        return OperationResult.<TransactionModel>builder()
                .addReturnedValue(walletMapper.createTransactionModelFromTransactionEntity(savedTransaction))
                .build();
    }

    @Override
    public void afterExecution(OperationResult<TransactionModel> response) {
    }
}
