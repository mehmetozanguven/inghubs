package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel;

import lombok.Getter;

@Getter
public enum AppRole {
    CUSTOMER("CUSTOMER", "ROLE_CUSTOMER"),
    EMPLOYEE( "EMPLOYEE", "ROLE_EMPLOYEE")
    ;
    public final String plainRole;
    public final String withRoleSuffix;

    AppRole(String plainRole, String withRoleSuffix) {
        this.plainRole = plainRole;
        this.withRoleSuffix = withRoleSuffix;
    }

    public static AppRole findByPlainRole(String plainRole) {
        for (AppRole each : AppRole.values()) {
            if (plainRole.equals(each.plainRole)) {
                return each;
            }
        }
        throw new IllegalArgumentException("Unknown appRole :: " + plainRole);
    }
}
