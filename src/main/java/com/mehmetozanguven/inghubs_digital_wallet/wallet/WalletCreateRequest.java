package com.mehmetozanguven.inghubs_digital_wallet.wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record WalletCreateRequest(
        String walletName,
        Boolean activeForShopping,
        Boolean activeForWithdraw,
        FinancialMoney financialMoney,
        String walletOwnerId
) {
}
