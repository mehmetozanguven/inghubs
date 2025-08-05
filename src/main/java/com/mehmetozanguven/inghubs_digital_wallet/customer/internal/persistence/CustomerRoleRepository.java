package com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRoleRepository extends JpaRepository<CustomerRole, String> {
}
