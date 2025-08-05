package com.mehmetozanguven.inghubs_digital_wallet.core.configuration;


import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionEvent;
import com.mehmetozanguven.inghubs_digital_wallet.core.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
@EnableKafka
public class KafkaConfiguration implements KafkaListenerConfigurer  {
    private final KafkaProperties kafkaProperties;
    private final LocalValidatorFactoryBean validator;

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(this.validator);
    }

    public Map<String, Object> jsonProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }

    @Bean
    public KafkaTemplate<String, TransactionEvent> transactionEventKafkaTemplate() {
        ProducerFactory<String, TransactionEvent> producerFactory = new DefaultKafkaProducerFactory<>(jsonProducerConfig());
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> transactionEventConcurrentKafkaListenerContainerFactory() {
        JsonDeserializer<TransactionEvent> deserializer = new JsonDeserializer<>(TransactionEvent.class);
        deserializer.setRemoveTypeHeaders(true);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(true);

        Map<String, Object> consumerMap = new HashMap<>();
        consumerMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        consumerMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        consumerMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "500"); // ms

        ConsumerFactory<String, TransactionEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerMap,
                new StringDeserializer(),
                deserializer
        );
        ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new TransactionEventErrorHandler());
        return factory;
    }
}
