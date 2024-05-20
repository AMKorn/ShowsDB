package com.andreas.showsdb.messaging;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collections;

@Configuration
public class KafkaTopicConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Collections.singletonMap(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress
        ));
    }

    @Bean
    public NewTopic novelties() {
        return new NewTopic("novelties", 1, (short) 1);
    }

    @Bean
    public NewTopic batchOrder() {
        return new NewTopic("batch-order", 1, (short) 1);
    }
}
