package com.mehmetozanguven.inghubs_digital_wallet.customer.internal;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiException;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerRoleModel;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.mapper.CustomerMapper;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.Customer;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.CustomerRepository;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.CustomerRole;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.Customer_;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterWithEmailPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerInternalService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public String createCustomer(RegisterWithEmailPasswordRequest registerWithEmailPasswordRequest, Set<CustomerRoleModel> roles) {
        Optional<Customer> inDb = customerRepository.findOne((Specification<Customer>) (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Customer_.email), registerWithEmailPasswordRequest.getEmail()));
        if (inDb.isPresent()) {
            throw new ApiException(ApiErrorInfo.CUSTOMER_ALREADY_EXISTS);
        }

        Set<CustomerRole> customerRoles = customerMapper.fromRoleModels(roles);
        Customer newCustomer = customerMapper.createNewCustomer(registerWithEmailPasswordRequest, customerRoles);
        newCustomer.addUserRole(CustomerRole.builder().appRole(AppRole.CUSTOMER).build());
        newCustomer = customerRepository.save(newCustomer);
        return newCustomer.getEmail();
    }
}
