package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type;


import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiException;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;

public interface TransactionTypeStrategy {
    OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext context) throws ApiException;
}
