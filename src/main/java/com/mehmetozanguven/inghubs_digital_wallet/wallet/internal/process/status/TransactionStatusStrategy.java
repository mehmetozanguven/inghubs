package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;


import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;

public interface TransactionStatusStrategy {

    OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext context);
}
