package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal;

import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.core.mapper.SwaggerValueMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.TransactionModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletCreateRequest;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.business.CreateNewTransactionUseCase;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.business.CreateWalletUseCase;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.*;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletInternalService {
    private final CreateWalletUseCase createWalletUseCase;
    private final CreateNewTransactionUseCase createNewTransactionUseCase;
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final TransactionRepository transactionRepository;

    public WalletResponse createWalletForCustomer(WalletCreateApiRequest walletCreateApiRequest, String customerId) {
        CurrencyType currencyType = SwaggerValueMapper.currencyTypeFromApiCurrencyType(walletCreateApiRequest.getCurrencyType());
        FinancialMoney zero = FinancialMoney.builder().currency(currencyType).amount(BigDecimal.ZERO).build();
        WalletCreateRequest createRequest = walletMapper.createFromApiRequest(walletCreateApiRequest, customerId, zero);
        OperationResult<WalletModel> result = createWalletUseCase.applyBusiness(createRequest);
        result.validateResult();
        return walletMapper.createApiResponseFromWalletModel(result.getReturnedValue());
    }

    public WalletModel getCustomerWallet(String customerId, String walletId) {
        Optional<Wallet> customerWallet = walletRepository.findOne(
                (Specification<Wallet>) (root, query, criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(
                            criteriaBuilder.equal(root.get(Wallet_.customerId), customerId)
                    );
                    predicates.add(
                            criteriaBuilder.equal(root.get(Wallet_.id), walletId)
                    );
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
        );

        return (customerWallet.map(
                walletMapper::createModelFromEntity
        ).orElseThrow());
    }

    public List<WalletModel> getCustomerWallets(String customerId, ApiCurrencyCode currency, BigDecimal balanceAmountEqualOrHigherThan) {
        Optional<CurrencyType> currencyType = Optional.ofNullable(currency)
                .map(SwaggerValueMapper::currencyTypeFromApiCurrencyType);
        Optional<BigDecimal> balanceAmountEqualOrHigherThanValue = Optional.ofNullable(balanceAmountEqualOrHigherThan);

        List<Wallet> customerWallets = walletRepository.findAll(
                (Specification<Wallet>) (root, query, criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(
                            criteriaBuilder.equal(root.get(Wallet_.customerId), customerId)
                    );
                    currencyType.ifPresent(filterCurrency ->
                            predicates.add(criteriaBuilder.equal(root.get(Wallet_.currencyType), filterCurrency))
                    );
                    balanceAmountEqualOrHigherThanValue.ifPresent(filteredAmount ->
                            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Wallet_.balance), filteredAmount))
                    );
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

                }
        );
        return walletMapper.createModelsFromEntities(customerWallets);
    }



    public WalletTransactionsResponse getListOfTransactionsInWallet(String customerId, String walletId, PageRequest pageRequest, ApiTransactionStatus apiTransactionStatus) {
        Optional<TransactionStatus> transactionStatus = Optional.ofNullable(apiTransactionStatus)
                .map(ApiTransactionStatus::getValue)
                .map(TransactionStatus::findByClientValue);

        Page<Transaction> walletsInPage = transactionRepository.findAll((Specification<Transaction>) (root, query, criteriaBuilder) -> {
            Join<Transaction, Wallet> walletJoin = root.join(Transaction_.wallet);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    criteriaBuilder.equal(walletJoin.get(Wallet_.customerId), customerId)
            );
            predicates.add(
                    criteriaBuilder.equal(walletJoin.get(Wallet_.id), walletId)
            );
            transactionStatus.ifPresent(value ->
                    predicates.add(criteriaBuilder.equal(root.get(Transaction_.transactionStatus), value))
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);

        List<TransactionResponse> responses = walletsInPage.stream().map(
                transaction -> {
                    TransactionModel transactionModel = walletMapper.createTransactionModelFromTransactionEntity(transaction);
                    return walletMapper.createTransactionResponseFromTransactionModel(transactionModel);
                }
        ).toList();
        return new WalletTransactionsResponse()
                .totalElements(walletsInPage.getTotalElements())
                .page(walletsInPage.getNumber())
                .size(walletsInPage.getSize())
                .totalPages(walletsInPage.getTotalPages())
                .data(responses)
                ;
    }

    public TransactionResponse createDepositTransaction(DepositApiRequest depositApiRequest, String customerId) {
        CreateNewTransactionUseCase.CreateTransactionRequest request = walletMapper.createDepositTransactionRequest(depositApiRequest, TransactionType.DEPOSIT, TransactionInfo.builder().build(), customerId);
        return createTransaction(request);
    }

    public TransactionResponse createWithdrawTransaction(WithdrawApiRequest withdrawApiRequest, String customerId) {
        CreateNewTransactionUseCase.CreateTransactionRequest request = walletMapper.createWithdrawTransactionRequest(withdrawApiRequest, TransactionType.WITHDRAW, TransactionInfo.builder().build(), customerId);
        return createTransaction(request);
    }

    private TransactionResponse createTransaction(CreateNewTransactionUseCase.CreateTransactionRequest request) {
        OperationResult<TransactionModel> createdTransaction = createNewTransactionUseCase.applyBusiness(request);
        createdTransaction.validateResult();
        TransactionModel created = createdTransaction.getReturnedValue();
        return walletMapper.createTransactionResponseFromTransactionModel(created);
    }
}
