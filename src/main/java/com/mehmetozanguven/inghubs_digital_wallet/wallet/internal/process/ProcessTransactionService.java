package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process;


import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionEvent;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.TransactionModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessTransactionService {
    private final ProcessTransactionUseCase processTransactionUseCase;
    private final WalletMapper transactionMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5000)
    public OperationResult<TransactionModel> processTransaction(TransactionEvent transactionEvent) {
        try {
            OperationResult<Transaction> result = processTransactionUseCase.applyBusiness(transactionEvent);
            result.validateResult();
            return OperationResult.<TransactionModel>builder()
                    .addReturnedValue(transactionMapper.createTransactionModelFromTransactionEntity(result.getReturnedValue()))
                    .build();
        } catch (Exception ex) {
            log.error("processTransaction exception. Rollback", ex);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationResult.<TransactionModel>builder()
                    .addException(ApiErrorInfo.TRANSACTION_OPERATION_FAILED)
                    .build();
        }
    }
}
