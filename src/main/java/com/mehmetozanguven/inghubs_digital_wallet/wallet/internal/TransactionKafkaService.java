package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal;

import com.mehmetozanguven.inghubs_digital_wallet.core.OperationResult;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionEvent;
import com.mehmetozanguven.inghubs_digital_wallet.core.properties.KafkaProperties;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.TransactionModel;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.ProcessTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionKafkaService {
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, TransactionEvent> transactionEventKafkaTemplate;
    private final ProcessTransactionService processTransactionService;

    @KafkaListener(topics = {"${app.kafka.transactionTopic}"}, groupId = "${app.kafka.groupId}", concurrency = "1", containerFactory = "transactionEventConcurrentKafkaListenerContainerFactory")
    public void listenTransactionEvent(@Payload @Valid TransactionEvent transactionEvent, Acknowledgment ack) {
        log.info("Processing transaction with id: {} in thread: {}\n\n", transactionEvent.transactionId(), Thread.currentThread().getName());
        OperationResult<TransactionModel> result = processTransactionService.processTransaction(transactionEvent);
        ack.acknowledge();
    }

    public void publishTransactionEvent(String key, TransactionEvent transactionEvent) {
        CompletableFuture<SendResult<String, TransactionEvent>> resultFuture = transactionEventKafkaTemplate.send(
            kafkaProperties.getTransactionTopic(), key, transactionEvent
        );

        resultFuture.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Kafka message is published");
            } else {
                log.error("An error occurred", exception);
            }
        });
    }
}
