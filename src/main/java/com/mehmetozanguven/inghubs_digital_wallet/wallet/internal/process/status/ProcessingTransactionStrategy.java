package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;


import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.properties.TransactionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@TransactionStatusHandler(TransactionStatus.PROCESSING)
public class ProcessingTransactionStrategy implements TransactionStatusStrategy {
    private final TransactionProperties transactionProperties;


    @Override
    public OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext transactionContext) {
        Transaction transaction = transactionContext.givenTransaction();

        FinancialMoney transactionAmount = transaction.getTransactionAmount();
        boolean checkMaxAllowedAmount = FinancialMoney.isMoreThanGivenAmount(transactionAmount, transactionProperties.getMaxAllowedAmountPerTransaction());

        if (checkMaxAllowedAmount) {
            transaction.setTransactionStatus(TransactionStatus.PENDING);
            transaction.setTransactionInfo(TransactionInfo.builder().code("-99").message("Waiting for approval").build());
        } else {
            transaction.setTransactionStatus(TransactionStatus.APPROVED);
        }
        return OperationResult.<ProcessTransactionContext>builder()
                .addReturnedValue(transactionContext)
                .build();
    }
}
