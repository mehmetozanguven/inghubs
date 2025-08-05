package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel;

import lombok.Getter;

@Getter
public enum CurrencyType  {
    UNKNOWN( "UNKNOWN", "UNKNOWN"),
    TRY( "TRY", "TRY"),
    USD("USD", "USD"),
    EURO("EURO", "EURO"),
    ;
    public final String dbValue;
    public final String value;

    CurrencyType(String dbValue, String value) {
        this.dbValue = dbValue;
        this.value = value;
    }

    public static CurrencyType findByClientValue(String givenValue) {
        for (CurrencyType each : CurrencyType.values()) {
            if (givenValue.equals(each.value)) {
                return each;
            }
        }
        return CurrencyType.UNKNOWN;
    }

    public static CurrencyType findByDBValue(String givenValue) {
        for (CurrencyType each : CurrencyType.values()) {
            if (givenValue.equals(each.dbValue)) {
                return each;
            }
        }
        return CurrencyType.UNKNOWN;
    }
}
