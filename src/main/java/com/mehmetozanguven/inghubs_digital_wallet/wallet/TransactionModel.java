package com.mehmetozanguven.inghubs_digital_wallet.wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.DateOperation;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.ApiBaseModel;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.opposite_party_type.OppositePartyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.Date;

@Getter
@Setter
@SuperBuilder
public class TransactionModel extends ApiBaseModel {

    public record TransactionInfoModel(String code, String message){};

    private String walletId;
    private String walletName;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private OppositePartyType oppositePartyType;
    private FinancialMoney transactionAmount;
    private OffsetDateTime processedAt;
    private OffsetDateTime expirationTime;
    private TransactionInfoModel transactionInfoModel;
    private boolean transactionExpired;

}
