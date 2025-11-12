package com.cts.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.ride-requests}")
    private String rideRequestsTopic;

    @Value("${app.kafka.topics.booking-events}")
    private String bookingEventsTopic;

    @Bean
    public NewTopic rideRequestsTopic() {
        return TopicBuilder.name(rideRequestsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name(bookingEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}