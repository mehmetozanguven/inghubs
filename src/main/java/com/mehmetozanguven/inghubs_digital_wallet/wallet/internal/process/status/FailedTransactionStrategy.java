package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;



import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@TransactionStatusHandler(TransactionStatus.FAIL)
public class FailedTransactionStrategy implements TransactionStatusStrategy {

    @Override
    public OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext transactionContext) {
        log.error("Failed transaction should not processed again, transactionContext: {}", transactionContext);
        return OperationResult.<ProcessTransactionContext>builder()
                .addReturnedValue(transactionContext)
                .build();
    }
}
