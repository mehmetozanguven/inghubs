package com.mehmetozanguven.inghubs_digital_wallet.core.testcontainer;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String POSTGRES_DOCKER_NAME = "postgres:17-alpine";
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_DOCKER_NAME));

    private static final String KAFKA_DOCKER_NAME = "apache/kafka:3.9.1";
    private static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse(KAFKA_DOCKER_NAME));

    static {
        Startables.deepStart(POSTGRES, KAFKA).join();
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
                "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                "spring.datasource.username=" + POSTGRES.getUsername(),
                "spring.datasource.password=" + POSTGRES.getPassword(),
                "spring.test.database.replace=none",
                "spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers(),
                "app.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers()
        ).applyTo(ctx.getEnvironment());
    }
}
