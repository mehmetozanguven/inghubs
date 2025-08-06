package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionEvent;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.TransactionKafkaService;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.TransactionRepository;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Transaction_;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence.Wallet;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionPublisher {
    private final TransactionRepository transactionRepository;
    private final TransactionKafkaService transactionKafkaService;

    private List<Transaction> getTransactionInPage(PageRequest pageRequest, TransactionStatus transactionStatus) {
        Slice<Transaction> waitingTransactions = transactionRepository.findAll((Specification<Transaction>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    criteriaBuilder.equal(root.get(Transaction_.transactionStatus), transactionStatus)
            );
            predicates.add(
                    criteriaBuilder.isNull(root.get(Transaction_.processedAt))
            );

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest) ;
        return waitingTransactions.getContent();
    }

    private void processTransaction(TransactionStatus transactionStatus) {
        PageRequest pageRequest = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "createTimestampMilli"));
        List<Transaction> waitingTransactions = getTransactionInPage(pageRequest, transactionStatus);
        if (!waitingTransactions.isEmpty()) {
            log.info("Trying to process {} transactions", transactionStatus);
        }
        waitingTransactions.forEach(transaction -> {
            Wallet wallet = transaction.getWallet();
            transactionKafkaService.publishTransactionEvent(wallet.getId(), new TransactionEvent(transaction.getId()));
        });
    }

    @Scheduled(cron = "${app.scheduler.process-transactions}")
    @SchedulerLock(name = "processingTransactionSchedulerLock", lockAtMostFor = "3m", lockAtLeastFor = "3s")
    public void pushProcessingTransactions() {
        processTransaction(TransactionStatus.PROCESSING);
    }

    @Scheduled(cron = "${app.scheduler.process-transactions}")
    @SchedulerLock(name = "pendingTransactionSchedulerLock", lockAtMostFor = "3m", lockAtLeastFor = "3s")
    public void pushPendingTransactions() {
        processTransaction(TransactionStatus.PENDING);
    }

    @Scheduled(cron = "${app.scheduler.process-transactions}")
    @SchedulerLock(name = "approvedTransactionSchedulerLock", lockAtMostFor = "3m", lockAtLeastFor = "3s")
    public void pushApprovedFromPendingTransactions() {
        processTransaction(TransactionStatus.APPROVED_FROM_PENDING);
    }

    @Scheduled(cron = "${app.scheduler.process-transactions}")
    @SchedulerLock(name = "approvedTransactionSchedulerLock", lockAtMostFor = "3m", lockAtLeastFor = "3s")
    public void pushApprovedTransactions() {
        processTransaction(TransactionStatus.APPROVED);
    }
}
