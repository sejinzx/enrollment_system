package com.sejinzx.enrollmentSystem;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;

public abstract class KafkaMySqlContainerTest
        extends MySqlContainerTest {

    protected static final KafkaContainer KAFKA =
            new KafkaContainer(
                    "apache/kafka-native:3.8.0"
            );

    static {
        KAFKA.start();
    }

    @DynamicPropertySource
    static void kafkaProperties(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.kafka.bootstrap-servers",
                KAFKA::getBootstrapServers
        );

        registry.add(
                "spring.kafka.listener.auto-startup",
                () -> "true"
        );

        registry.add(
                "spring.kafka.admin.auto-create",
                () -> "true"
        );
    }
}