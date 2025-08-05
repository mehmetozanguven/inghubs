package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;


import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnknownTransactionStatusStrategy implements TransactionStatusStrategy {

    @Override
    public OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext transactionContext) {
        log.info("There is no action for the given transactionStatus: {}, TransactionContext: {}", transactionContext.givenTransaction().getTransactionStatus(), transactionContext);
        return OperationResult.<ProcessTransactionContext>builder()
                .addReturnedValue(transactionContext)
                .build();
    }
}
