package com.mehmetozanguven.inghubs_digital_wallet.customer;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.CustomerInternalService;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.mapper.CustomerMapper;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.Customer;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.CustomerRepository;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.Customer_;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterResponse;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterWithEmailPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerExternalService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerInternalService customerInternalService;

    public Optional<CustomerModel> findCustomerByEmailAddress(String emailAddress) {
        Optional<Customer> inDb = customerRepository.findOne((Specification<Customer>) (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Customer_.email), emailAddress));
        return inDb.map(
                customer -> customerMapper.modelFromCustomer(customer, true)
        );
    }

    public String createEmployee(RegisterWithEmailPasswordRequest registerWithEmailPasswordRequest) {
        return customerInternalService.createCustomer(registerWithEmailPasswordRequest, Set.of(CustomerRoleModel.builder().appRole(AppRole.EMPLOYEE).build()));
    }
}
