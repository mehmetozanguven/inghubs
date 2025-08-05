package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper;

import com.mehmetozanguven.inghubs_digital_wallet.core.DateOperation;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.core.mapper.*;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.TransactionModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletCreateRequest;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.business.CreateNewTransactionUseCase;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = SwaggerValueMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface WalletMapper {
    @Mappings(value = {
            @Mapping(target = "customerId", source = "walletOwnerId"),
            @Mapping(target = "walletName", source = "walletName"),
            @Mapping(target = "currencyType", source = "walletCreateRequest.financialMoney.currency"),
            @Mapping(target = "activeForShopping", source = "activeForShopping"),
            @Mapping(target = "activeForWithdraw", source = "activeForWithdraw"),
            @Mapping(target = "balance", ignore = true),
            @Mapping(target = "usableBalance", ignore = true),
    })
    Wallet createNewWallet(WalletCreateRequest walletCreateRequest);

    @Mappings(value = {
            @Mapping(target = "balance", expression = "java(wallet.getBalance())"),
            @Mapping(target = "usableBalance", expression = "java(wallet.getUsableBalance())")
    })
    WalletModel createModelFromEntity(Wallet wallet);

    List<WalletModel> createModelsFromEntities(Collection<Wallet> accounts);

    @Mappings(value = {
            @Mapping(target = "walletOwnerId", source = "customerId"),
            @Mapping(target = "financialMoney", source = "initialWalletMoney"),
    })
    WalletCreateRequest createFromApiRequest(WalletCreateApiRequest walletCreateApiRequest, String customerId, FinancialMoney initialWalletMoney);

    @Mappings(value = {
            @Mapping(target = "usableBalance", source = "usableBalance", qualifiedBy = ApiMoneyFromFinancialMoney.class),
            @Mapping(target = "balance", source = "balance", qualifiedBy = ApiMoneyFromFinancialMoney.class),
    })
    WalletResponse createApiResponseFromWalletModel(WalletModel walletModel);

    List<WalletResponse> createApiResponsesFromWalletModels(Collection<WalletModel> walletModels);

    @Mappings(value = {
            @Mapping(target = "financialMoney", source = "depositApiRequest.money", qualifiedBy = FinanceMoneyFromApiMoney.class),
            @Mapping(target = "oppositePartyType", source = "depositApiRequest.sourceOfDeposit", qualifiedBy = OppositePartyTypeFromApiPartyType.class),
            @Mapping(target = "transactionInfo", source = "transactionInfo"),
            @Mapping(target = "transactionType", source = "transactionType"),
            @Mapping(target = "customerId", source = "customerId")
    })
    CreateNewTransactionUseCase.CreateTransactionRequest createDepositTransactionRequest(DepositApiRequest depositApiRequest,
                                                                                  TransactionType transactionType,
                                                                                  TransactionInfo transactionInfo,
                                                                                  String customerId);
    @Mappings(value = {
            @Mapping(target = "financialMoney", source = "withdrawApiRequest.money", qualifiedBy = FinanceMoneyFromApiMoney.class),
            @Mapping(target = "oppositePartyType", source = "withdrawApiRequest.sourceOfDeposit", qualifiedBy = OppositePartyTypeFromApiPartyType.class),
            @Mapping(target = "transactionInfo", source = "transactionInfo"),
            @Mapping(target = "transactionType", source = "transactionType"),
            @Mapping(target = "customerId", source = "customerId")
    })
    CreateNewTransactionUseCase.CreateTransactionRequest createWithdrawTransactionRequest(WithdrawApiRequest withdrawApiRequest,
                                                                              TransactionType transactionType,
                                                                              TransactionInfo transactionInfo,
                                                                              String customerId);

    @Mappings(value = {
            @Mapping(target = "walletId", source = "savedTransaction.wallet.id"),
            @Mapping(target = "walletName", source = "savedTransaction.wallet.walletName"),
            @Mapping(target = "transactionAmount", expression = "java(savedTransaction.getTransactionAmount())"),
            @Mapping(target = "transactionType", source = "transactionType"),
            @Mapping(target = "transactionInfoModel", source = "transactionInfo"),
            @Mapping(target = "transactionExpired", expression = "java(savedTransaction.getIsTransactionExpired())"),
    })
    TransactionModel createTransactionModelFromTransactionEntity(Transaction savedTransaction);

    @Mappings(value = {
            @Mapping(target = "walletId", source = "transactionModel.walletId"),
            @Mapping(target = "transactionStatus", source = "transactionModel.transactionStatus", qualifiedBy = ApiTransactionStatusFromTransactionStatus.class),
            @Mapping(target = "transactionId", source = "transactionModel.id"),
            @Mapping(target = "amount", source = "transactionModel.transactionAmount", qualifiedBy = ApiMoneyFromFinancialMoney.class),
            @Mapping(target = "transactionType", source = "transactionModel.transactionType.value"),
            @Mapping(target = "code", source = "transactionModel.transactionInfoModel.code"),
            @Mapping(target = "message", source = "transactionModel.transactionInfoModel.message"),
            @Mapping(target = "expirationTime", source = "transactionModel.expirationTime", qualifiedBy = OffsetDateTimeToEpochMillis.class),
            @Mapping(target = "isExpired", source = "transactionModel.transactionExpired")
    })
    TransactionResponse createTransactionResponseFromTransactionModel(TransactionModel transactionModel);


}
