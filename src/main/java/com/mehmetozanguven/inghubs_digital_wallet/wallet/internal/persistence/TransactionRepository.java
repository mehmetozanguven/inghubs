package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("FROM Transaction ts JOIN FETCH ts.wallet WHERE ts.id = :transactionId ")
    Optional<Transaction> findByPessimisticLockBeforeExpiration(String transactionId);

}
