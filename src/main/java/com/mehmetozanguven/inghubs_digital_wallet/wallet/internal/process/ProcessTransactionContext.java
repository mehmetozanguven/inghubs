package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;

public record ProcessTransactionContext(Transaction givenTransaction) {

    public boolean isTransactionPending() {
        return this.givenTransaction.getTransactionStatus().equals(TransactionStatus.PENDING);
    }

    public boolean isTransactionFromPendingToApproved() {
        return this.givenTransaction.getTransactionStatus().equals(TransactionStatus.APPROVED_FROM_PENDING);
    }

    public boolean isApprovedTransaction() {
        return this.givenTransaction.getTransactionStatus().equals(TransactionStatus.APPROVED);
    }
}
