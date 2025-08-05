package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type;


import com.mehmetozanguven.inghubs_digital_wallet.core.DateOperation;
import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@TransactionTypeHandler(TransactionType.UNKNOWN)
public class UnknownStrategy implements TransactionTypeStrategy {

    @Override
    public OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext transactionContext) {
        Transaction transaction = transactionContext.givenTransaction();
        log.error("Unknown strategy, make transaction status failed");
        transaction.setTransactionStatus(TransactionStatus.FAIL);
        transaction.setTransactionInfo(TransactionInfo.builder().code("-1").message("Unknown transactionType").build());
        transaction.setProcessedAt(DateOperation.getOffsetNowAsUTC());

        return OperationResult.<ProcessTransactionContext>builder()
                .addReturnedValue(transactionContext)
                .build();
    }
}
