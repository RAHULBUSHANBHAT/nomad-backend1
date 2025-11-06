package com.cts.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.user-events}")
    private String userEventsTopic;

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(userEventsTopic)
                .partitions(3) 
                .replicas(1) 
                .build();
    }
}