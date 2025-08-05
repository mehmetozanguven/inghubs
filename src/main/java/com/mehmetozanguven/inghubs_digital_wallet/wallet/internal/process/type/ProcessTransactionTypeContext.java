package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import lombok.Builder;


@Builder
public class ProcessTransactionTypeContext {
    private FinancialMoney walletMoney;
    private FinancialMoney walletUsableMoney;

}
