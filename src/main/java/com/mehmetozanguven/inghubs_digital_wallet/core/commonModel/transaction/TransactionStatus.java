package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    UNKNOWN( "UNKNOWN", "UNKNOWN"),
    PROCESSING( "PROCESSING", "PROCESSING"),
    PENDING( "PENDING", "PENDING"),
    APPROVED_FROM_PENDING( "APPROVED_FROM_PENDING", "APPROVED_FROM_PENDING"),
    APPROVED( "APPROVED", "APPROVED"),
    FAIL("FAIL", "FAIL");
    public final String dbValue;
    public final String value;

    TransactionStatus(String dbValue, String value) {
        this.dbValue = dbValue;
        this.value = value;
    }

    public static TransactionStatus findByDBValue(String givenValue) {
        for (TransactionStatus each : TransactionStatus.values()) {
            if (givenValue.equals(each.dbValue)) {
                return each;
            }
        }
        return TransactionStatus.UNKNOWN;
    }

    public static TransactionStatus findByClientValue(String givenValue) {
        for (TransactionStatus each : TransactionStatus.values()) {
            if (givenValue.equals(each.value)) {
                return each;
            }
        }
        return TransactionStatus.UNKNOWN;
    }
}
