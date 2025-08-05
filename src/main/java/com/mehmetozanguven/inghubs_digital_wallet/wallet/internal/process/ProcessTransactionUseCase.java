package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process;

import com.mehmetozanguven.inghubs_digital_wallet.core.ApiApplyOperationResultLogic;
import com.mehmetozanguven.inghubs_digital_wallet.core.BusinessUseCase;
import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionEvent;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.WalletRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status.TransactionStatusStrategy;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status.TransactionStatusStrategyRegistry;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@BusinessUseCase
@RequiredArgsConstructor
public class ProcessTransactionUseCase implements ApiApplyOperationResultLogic<TransactionEvent, ProcessTransactionUseCase.ProcessTransactionInternalRequest, Transaction> {
    @Builder
    public record ProcessTransactionInternalRequest(Transaction transaction) {}

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionStatusStrategyRegistry statusStrategy;

    @Override
    public OperationResult<ProcessTransactionInternalRequest> logicBefore(TransactionEvent transactionEvent) {
        Optional<Transaction> inDB = transactionRepository.findByPessimisticLockBeforeExpiration(transactionEvent.transactionId());
        if (inDB.isEmpty()) {
            log.error("There is no transaction for the given ID: {}", transactionEvent.transactionId());
            return OperationResult.<ProcessTransactionInternalRequest>builder()
                    .addException(ApiErrorInfo.TRANSACTION_NOT_FOUND)
                    .build();
        }
        Transaction transaction = inDB.get();
        if (transaction.isTransactionExpired()) {
            return OperationResult.<ProcessTransactionInternalRequest>builder()
                    .addException(ApiErrorInfo.TRANSACTION_OPERATION_FAILED)
                    .build();
        }
        return OperationResult.<ProcessTransactionInternalRequest>builder()
                .addReturnedValue(ProcessTransactionInternalRequest.builder()
                        .transaction(transaction)
                        .build()
                )
                .build();
    }

    @Override
    public OperationResult<Transaction> executeLogic(ProcessTransactionInternalRequest processTransactionInternalRequest) {
        Transaction transaction = processTransactionInternalRequest.transaction();

        TransactionStatusStrategy foundStatusStrategy = statusStrategy.getHandler(transaction.getTransactionStatus());
        OperationResult<ProcessTransactionContext> response = foundStatusStrategy.doAction(
                new ProcessTransactionContext(transaction)
        );
        response.validateResult();
        ProcessTransactionContext processTransactionResponse = response.getReturnedValue();
        transactionRepository.save(processTransactionResponse.givenTransaction());
        walletRepository.save(processTransactionResponse.givenTransaction().getWallet());

        return OperationResult.<Transaction>builder()
                .addReturnedValue(transaction)
                .build();
    }

    @Override
    public void afterExecution(OperationResult<Transaction> response) {

    }
}
