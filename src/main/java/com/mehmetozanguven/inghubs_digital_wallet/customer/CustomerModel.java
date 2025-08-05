package com.mehmetozanguven.inghubs_digital_wallet.customer;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.ApiBaseModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder
public class CustomerModel extends ApiBaseModel {
    private String email;
    private String name;
    private String surname;
    private String tckn;
    private String password;
    @Builder.Default
    private Set<CustomerRoleModel> customerRoles = new HashSet<>();
    private boolean enabled;

    public List<String> getRolesAsStrings() {
        return getCustomerRoles().stream().map(roleModel -> roleModel.getAppRole().getWithRoleSuffix()).collect(Collectors.toList());
    }
}
