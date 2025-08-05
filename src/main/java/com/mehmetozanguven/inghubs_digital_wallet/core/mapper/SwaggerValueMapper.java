package com.mehmetozanguven.inghubs_digital_wallet.core.mapper;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.opposite_party_type.OppositePartyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.ApiCurrencyCode;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.ApiMoney;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.ApiPartyType;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.ApiTransactionStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class SwaggerValueMapper {

    @ApiMoneyFromFinancialMoney
    public static ApiMoney getApiMoney(FinancialMoney financialMoney) {
        ApiCurrencyCode appCurrencyCode = ApiCurrencyCode.fromValue(financialMoney.currency().value);
        return new ApiMoney()
                .currencyType(appCurrencyCode)
                .money(financialMoney.amount())
                ;
    }

    @FinanceMoneyFromApiMoney
    public static FinancialMoney fromApiMoney(ApiMoney apiMoney) {
        return new FinancialMoney(apiMoney.getMoney(), CurrencyType.findByClientValue(apiMoney.getCurrencyType().getValue()));
    }

    @OppositePartyTypeFromApiPartyType
    public static OppositePartyType fromApiPartyType(ApiPartyType partyType) {
        return OppositePartyType.findByClientValue(partyType.getValue());
    }

    @ApiTransactionStatusFromTransactionStatus
    public static ApiTransactionStatus fromTransactionStatus(TransactionStatus transactionStatus) {
        return ApiTransactionStatus.fromValue(transactionStatus.getValue());
    }

    public static CurrencyType currencyTypeFromApiCurrencyType(ApiCurrencyCode apiCurrencyCode) {
        return CurrencyType.findByClientValue(apiCurrencyCode.getValue());
    }
}
