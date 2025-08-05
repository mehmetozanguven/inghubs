package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type;

import com.mehmetozanguven.inghubs_digital_wallet.core.DateOperation;
import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiException;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@TransactionTypeHandler(TransactionType.DEPOSIT)
public class DepositStrategy implements TransactionTypeStrategy {
    @Override
    public OperationResult<ProcessTransactionContext> doAction(ProcessTransactionContext transactionContext) {
        Transaction transaction = transactionContext.givenTransaction();
        TransactionInfo transactionInfo = transaction.getTransactionInfo();
        try {
            Wallet wallet = transaction.getWallet();
            FinancialMoney transactionAmount = transaction.getTransactionAmount();
            FinancialMoney usableBalanceAfterDeposit = FinancialMoney.addMoney(wallet.getUsableBalance(), transactionAmount);
            FinancialMoney balanceAfterDeposit = FinancialMoney.addMoney(wallet.getBalance(), transactionAmount);

            if (transactionContext.isApprovedTransaction()) {
                wallet.setUsableBalance(usableBalanceAfterDeposit);
                wallet.setBalance(balanceAfterDeposit);

                transactionInfo.setCode("");
                transactionInfo.setMessage("");
                transaction.setProcessedAt(DateOperation.getOffsetNowAsUTC());
            }

            if (transactionContext.isTransactionPending()) {
                wallet.setBalance(balanceAfterDeposit);
            }

            if (transactionContext.isTransactionFromPendingToApproved()) {
                wallet.setUsableBalance(usableBalanceAfterDeposit);

                transactionInfo.setCode("");
                transactionInfo.setMessage("");
                transaction.setTransactionStatus(TransactionStatus.APPROVED);
                transaction.setProcessedAt(DateOperation.getOffsetNowAsUTC());
            }

            return OperationResult.<ProcessTransactionContext>builder()
                    .addReturnedValue(transactionContext)
                    .build();

        } catch (ApiException ex) {
            transactionInfo.setMessage(ex.getMessage());
            transactionInfo.setCode(ex.getCode());

            transaction.setTransactionStatus(TransactionStatus.FAIL);
            transaction.setProcessedAt(DateOperation.getOffsetNowAsUTC());

            return OperationResult.<ProcessTransactionContext>builder()
                    .addReturnedValue(transactionContext)
                    .build();
        } catch (Exception ex) {
            log.error("Error", ex);
            transactionInfo.setMessage(ApiErrorInfo.TRANSACTION_OPERATION_FAILED.getMessage());
            transactionInfo.setCode(ApiErrorInfo.TRANSACTION_OPERATION_FAILED.getCode());
            transaction.setTransactionStatus(TransactionStatus.FAIL);
            transaction.setProcessedAt(DateOperation.getOffsetNowAsUTC());

            return OperationResult.<ProcessTransactionContext>builder()
                    .addReturnedValue(transactionContext)
                    .build();
        }
    }
}
