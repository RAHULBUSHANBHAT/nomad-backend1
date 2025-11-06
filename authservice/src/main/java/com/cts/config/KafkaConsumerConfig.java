package com.cts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Enables Kafka listener capabilities in this Spring application.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    // We don't need to define any beans here,
    // as Spring Boot can autoconfigure the consumer factory
    // from our application.yml properties.
}