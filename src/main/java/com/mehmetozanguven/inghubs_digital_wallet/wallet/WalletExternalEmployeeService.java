package com.mehmetozanguven.inghubs_digital_wallet.wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionEvent;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.TransactionKafkaService;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.mapper.WalletMapper;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction_;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.ApiTransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.EmployeeTransactionsResponse;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.TransactionResponse;
import jakarta.persistence.criteria.Predicate;
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
        transactionTemplate.executeWithoutResult(status -> {
            Optional<Transaction> inDB = transactionRepository.findByPessimisticLockBeforeExpiration(transactionId);
            Transaction transaction = inDB.orElseThrow();
            transaction.setTransactionStatus(TransactionStatus.APPROVED_FROM_PENDING);
            transactionRepository.save(transaction);
        });
        TransactionEvent transactionEvent = new TransactionEvent(transactionId);
        transactionKafkaService.publishTransactionEvent(walletId, transactionEvent);
        return true;
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
