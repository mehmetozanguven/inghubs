package com.mehmetozanguven.inghubs_digital_wallet.wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.ApiBaseModel;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class WalletModel extends ApiBaseModel {
    private String customerId;
    private String walletName;
    private Boolean activeForShopping;
    private Boolean activeForWithdraw;
    private FinancialMoney balance;
    private FinancialMoney usableBalance;
}
