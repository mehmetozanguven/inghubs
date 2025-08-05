package com.mehmetozanguven.inghubs_digital_wallet.core.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

@Slf4j
public class TransactionEventErrorHandler implements CommonErrorHandler {

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, MessageListenerContainer container) {
        handleException(thrownException, consumer);
        return true;
    }

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
        handleException(thrownException, consumer);
    }


    private void handleException(Exception exception, Consumer<?, ?> consumer) {
        log.error("TransactionEventErrorHandler exception", exception);
        if (exception instanceof RecordDeserializationException) {
            RecordDeserializationException rde = (RecordDeserializationException) exception;
            consumer.seek(rde.topicPartition(), rde.offset() + 1);
            consumer.commitSync();
        } else {
            log.error("TransactionEventErrorHandler not handled", exception);
        }
    }
}
