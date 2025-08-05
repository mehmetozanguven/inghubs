package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.opposite_party_type;

import lombok.Getter;

@Getter
public enum OppositePartyType {
    UNKNOWN( "UNKNOWN", "UNKNOWN"),
    IBAN( "IBAN", "IBAN"),
    PAYMENT( "PAYMENT", "PAYMENT")
    ;
    public final String dbValue;
    public final String value;

    OppositePartyType(String dbValue, String value) {
        this.dbValue = dbValue;
        this.value = value;
    }

    public static OppositePartyType findByDBValue(String givenValue) {
        for (OppositePartyType each : OppositePartyType.values()) {
            if (givenValue.equals(each.dbValue)) {
                return each;
            }
        }
        return OppositePartyType.UNKNOWN;
    }

    public static OppositePartyType findByClientValue(String givenValue) {
        for (OppositePartyType each : OppositePartyType.values()) {
            if (givenValue.equals(each.value)) {
                return each;
            }
        }
        return OppositePartyType.UNKNOWN;
    }
}
