package com.cts.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer; 

import com.cts.dto.kafka.UserEventDto;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Bean
    public ConsumerFactory<String, UserEventDto> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Key deserializer is simple String
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // For the value, wrap JsonDeserializer in ErrorHandlingDeserializer
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        
        // Tell JsonDeserializer what type to deserialize into and trust both package paths
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserEventDto.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, 
                "com.cts.dto.kafka," +      // Auth service package
                "com.cts.user.dto.kafka");  // User service package
        
        // Add type mappings to help with deserialization
        props.put(JsonDeserializer.TYPE_MAPPINGS, 
                "com.cts.user.dto.kafka.UserEventDto:com.cts.dto.kafka.UserEventDto");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        // Disable auto-commit to prevent missing messages on errors
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Add these to help with debugging
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "userevent:com.cts.dto.kafka.UserEventDto");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEventDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserEventDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Add error handler with detailed exception logging
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            log.error("Failed to process Kafka record. Topic: {}, Partition: {}, Offset: {}", 
                record.topic(), record.partition(), record.offset(), exception);
            
            // Get the root cause for more details
            Throwable rootCause = exception;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            log.error("Root cause: {}", rootCause.getMessage());
            
            // If it's a deserialization error, try to log the raw value
            if (record.value() != null) {
                log.error("Raw record value: {}", record.value());
            }
        });
        
        // Don't retry on specific exceptions
        errorHandler.addNotRetryableExceptions(org.springframework.kafka.support.serializer.DeserializationException.class);
        factory.setCommonErrorHandler(errorHandler);
        
        return factory;
    }
}