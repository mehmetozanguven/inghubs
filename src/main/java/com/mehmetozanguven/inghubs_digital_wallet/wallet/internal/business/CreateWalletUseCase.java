package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.business;

import com.mehmetozanguven.inghubs_digital_wallet.core.ApiApplyOperationResultLogic;
import com.mehmetozanguven.inghubs_digital_wallet.core.BusinessUseCase;
import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletCreateRequest;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.WalletModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.WalletRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet_;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@BusinessUseCase
@RequiredArgsConstructor
public class CreateWalletUseCase implements ApiApplyOperationResultLogic<WalletCreateRequest, WalletCreateRequest, WalletModel> {
    private static final int MAX_ALLOWED_WALLETS_PER_CUSTOMER = 5;

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    public OperationResult<WalletCreateRequest> logicBefore(WalletCreateRequest walletCreateRequest) {

        long totalWalletForCustomer = walletRepository.count((Specification<Wallet>) (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Wallet_.customerId), walletCreateRequest.walletOwnerId()));
        if (totalWalletForCustomer >= MAX_ALLOWED_WALLETS_PER_CUSTOMER) {
            return OperationResult.<WalletCreateRequest>builder()
                    .addException(ApiErrorInfo.WALLET_INVALID_REQUEST, "Can not create more than " + MAX_ALLOWED_WALLETS_PER_CUSTOMER + " wallets")
                    .build();
        }

        boolean isWalletAlreadyCreated = walletRepository.exists((Specification<Wallet>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    criteriaBuilder.equal(root.get(Wallet_.customerId), walletCreateRequest.walletOwnerId())
            );
            predicates.add(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(Wallet_.walletName)), "%" + walletCreateRequest.walletName() + "%"
                    )
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        if (isWalletAlreadyCreated) {
            return OperationResult.<WalletCreateRequest>builder()
                    .addException(ApiErrorInfo.WALLET_INVALID_REQUEST, "Wallet already created")
                    .build();
        }

        return OperationResult.<WalletCreateRequest>builder()
                .addReturnedValue(walletCreateRequest)
                .build();
    }

    @Override
    public OperationResult<WalletModel> executeLogic(WalletCreateRequest walletCreateRequest) {
        Wallet newWallet = walletMapper.createNewWallet(walletCreateRequest);
        newWallet = walletRepository.save(newWallet);
        WalletModel walletModel = walletMapper.createModelFromEntity(newWallet);
        return OperationResult.<WalletModel>builder()
                .addReturnedValue(walletModel)
                .build();
    }

    @Override
    public void afterExecution(OperationResult<WalletModel> response) {

    }
}
