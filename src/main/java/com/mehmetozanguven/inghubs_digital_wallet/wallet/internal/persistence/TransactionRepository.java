package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("FROM Transaction ts JOIN FETCH ts.wallet tw WHERE ts.id = :transactionId AND ts.expirationTime >= :now AND ts.processedAt IS NULL ")
    Optional<Transaction> findTransactionToProcess(String transactionId, OffsetDateTime now);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("FROM Transaction ts JOIN FETCH ts.wallet tw WHERE tw.id = :walletId AND ts.id = :transactionId AND ts.transactionStatus = :transactionStatus")
    Optional<Transaction> findByPessimisticLockForApprove(String walletId, String transactionId, TransactionStatus transactionStatus);

}
