package com.mehmetozanguven.inghubs_digital_wallet.wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.DateOperation;
import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionEvent;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiException;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.TransactionKafkaService;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.business.CreateNewTransactionUseCase;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionInfo;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction_;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.ApiTransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.EmployeeTransactionsResponse;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.TransactionResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@PreAuthorize("hasRole('ROLE_EMPLOYEE')")
public class WalletExternalEmployeeService {
    private final TransactionKafkaService transactionKafkaService;
    private final TransactionTemplate transactionTemplate;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;

    public WalletExternalEmployeeService(TransactionKafkaService transactionKafkaService,
                                         PlatformTransactionManager transactionManager, TransactionRepository transactionRepository, WalletMapper walletMapper) {
        this.transactionKafkaService = transactionKafkaService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionRepository = transactionRepository;
        this.walletMapper = walletMapper;
        transactionTemplate.setTimeout(5000);
    }

    public boolean approveTransaction(String walletId, String transactionId) {
        try {
            OperationResult<Boolean> result = transactionTemplate.execute(status -> {
                Optional<Transaction> inDB = transactionRepository.findByPessimisticLockForApprove(walletId, transactionId, TransactionStatus.PENDING);
                if (inDB.isEmpty()) {
                    return OperationResult.<Boolean>builder()
                            .addException(ApiErrorInfo.TRANSACTION_NOT_FOUND)
                            .build();
                }
                Transaction transaction = inDB.get();
                if (transaction.getIsTransactionExpired()) {
                    transaction.setProcessedAt(DateOperation.getOffsetNowAsUTC());
                    transaction.setTransactionStatus(TransactionStatus.FAIL);
                    transaction.setTransactionInfo(TransactionInfo.builder()
                                    .code("-1")
                                    .message("Failed because it is expired")
                            .build());
                    transactionRepository.save(transaction);

                    return OperationResult.<Boolean>builder()
                            .addException(ApiErrorInfo.TRANSACTION_EXPIRED)
                            .build();
                } else {
                    transaction.setTransactionStatus(TransactionStatus.APPROVED_FROM_PENDING);
                    transaction.setProcessedAt(null);
                    transactionRepository.save(transaction);
                    return OperationResult.<Boolean>builder()
                            .addReturnedValue(true)
                            .build();
                }
            });
            result.validateResult();
            TransactionEvent transactionEvent = new TransactionEvent(transactionId);
            transactionKafkaService.publishTransactionEvent(walletId, transactionEvent);
            return true;
        } catch (ApiException ex) {
            log.error("approveTransaction apiException", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("approveTransaction", ex);
            throw new ApiException(ApiErrorInfo.TRANSACTION_OPERATION_FAILED);
        }

    }

    public EmployeeTransactionsResponse getListOfTransactions(Integer page, Integer size, ApiTransactionStatus apiTransactionStatus) {
        Optional<TransactionStatus> transactionStatus = Optional.ofNullable(apiTransactionStatus)
                .map(ApiTransactionStatus::getValue)
                .map(TransactionStatus::findByClientValue);
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Transaction> walletsInPage = transactionRepository.findAll((Specification<Transaction>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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

        return new EmployeeTransactionsResponse()
                .totalElements(walletsInPage.getTotalElements())
                .page(walletsInPage.getNumber())
                .size(walletsInPage.getSize())
                .totalPages(walletsInPage.getTotalPages())
                .data(responses)
                ;
    }
}
