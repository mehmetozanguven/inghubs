package com.mehmetozanguven.inghubs_digital_wallet.customer.internal.mapper;

import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerModel;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerRoleModel;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.Customer;
import com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence.CustomerRole;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.RegisterWithEmailPasswordRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mappings(value = {
            @Mapping(
                    target = "customerRoles", source = "customerRoles"
            )
    })
    Customer createNewCustomer(RegisterWithEmailPasswordRequest registerWithEmailPasswordRequest, Set<CustomerRole> customerRoles);

    @Mappings(value = {
            @Mapping(target = "enabled", source = "isEnabled")
    })
    CustomerModel modelFromCustomer(Customer customer, boolean isEnabled);

    CustomerRole fromRoleModel(CustomerRoleModel customerRoleModel);

    Set<CustomerRole> fromRoleModels(Collection<CustomerRoleModel> customerRoleModels);

}
