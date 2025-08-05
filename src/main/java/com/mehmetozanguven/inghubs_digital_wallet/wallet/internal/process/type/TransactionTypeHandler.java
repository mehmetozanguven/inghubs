package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type;


import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionTypeHandler {
    TransactionType value();

}
