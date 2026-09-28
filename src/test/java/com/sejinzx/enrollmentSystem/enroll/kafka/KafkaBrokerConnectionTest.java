package com.sejinzx.enrollmentSystem.enroll.kafka;

import com.sejinzx.enrollmentSystem.KafkaMySqlContainerTest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class KafkaBrokerConnectionTest extends KafkaMySqlContainerTest {

    @Test
    void kafkaConnectionTest() throws Exception {

        try (AdminClient admin = AdminClient.create(
                Map.of(
                        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                        KAFKA.getBootstrapServers()
                )
        )) {

            Set<String> topics =
                    admin.listTopics()
                            .names()
                            .get(10, TimeUnit.SECONDS);

            assertThat(topics)
                    .contains("enrollment-request");
        }
    }
}