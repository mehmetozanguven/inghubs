package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;


import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionStatusHandler {
    TransactionStatus value();

}
