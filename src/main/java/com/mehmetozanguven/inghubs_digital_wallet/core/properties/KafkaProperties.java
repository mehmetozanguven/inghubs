package com.mehmetozanguven.inghubs_digital_wallet.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {

    private String groupId;
    private String bootstrapServers;
    private String transactionTopic;
}
