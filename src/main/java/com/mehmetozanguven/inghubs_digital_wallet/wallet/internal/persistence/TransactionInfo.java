package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionInfo {
    private String code;
    private String message;
}
