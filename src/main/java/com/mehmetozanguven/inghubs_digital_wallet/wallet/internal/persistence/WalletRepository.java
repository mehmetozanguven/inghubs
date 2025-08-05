package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String>, JpaSpecificationExecutor<Wallet> {
}
