package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;


import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type.TransactionTypeStrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@TransactionStatusHandler(TransactionStatus.APPROVED_FROM_PENDING)
public class ApprovedFromPendingTransactionStrategy implements TransactionStatusStrategy {
    private final TransactionTypeStrategyRegistry typeStrategyRegistry;

    @Override
    public OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext transactionContext) {
        return typeStrategyRegistry.getHandler(transactionContext.givenTransaction().getTransactionType()).doAction(transactionContext);
    }
}
