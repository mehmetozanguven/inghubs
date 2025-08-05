package com.mehmetozanguven.inghubs_digital_wallet.customer;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.ApiBaseModel;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class CustomerRoleModel extends ApiBaseModel {
    private AppRole appRole;
}
