package com.cts.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.cts.dto.kafka.UserEventDto;
import com.cts.model.UserCredential;
import com.cts.repository.UserCredentialRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserEventConsumer {

    @Autowired
    private UserCredentialRepository repository;

    /**
     * Listens for messages on the 'user-events-topic'.
     */
    @KafkaListener(
        topics = "${app.kafka.topics.user-events}", 
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory")
    public void handleUserEvent(UserEventDto eventDto) {
        log.info("Received Kafka message: eventType={}, userId={}, email={}", 
                eventDto.getEventType(), eventDto.getUserId(), eventDto.getEmail());
        
        try {
            if ("USER_CREATED".equals(eventDto.getEventType())) {
                log.debug("Processing USER_CREATED event for user: {}", eventDto.getEmail());
                
                if(repository.existsById(eventDto.getUserId())) {
                    log.warn("UserCredential for user {} already exists. Skipping creation.", eventDto.getUserId());
                    return;
                }

                log.debug("Creating new UserCredential with userId={}, email={}, role={}", 
                        eventDto.getUserId(), eventDto.getEmail(), eventDto.getRole());

                UserCredential credential = new UserCredential(
                        eventDto.getUserId(),
                        eventDto.getEmail(),
                        eventDto.getPassword(), // This should be the hashed password from user-service
                        eventDto.getRole()
                );

                try {
                    UserCredential saved = repository.save(credential);
                    log.info("Successfully created UserCredential. ID: {}, Email: {}", saved.getUserId(), saved.getEmail());
                } catch (Exception e) {
                    log.error("Database error saving UserCredential: {}", e.getMessage(), e);
                    throw e; // Re-throw so the container can handle it
                }
                log.info("Successfully created local UserCredential for: {}", credential.getEmail());
            } 
        } catch (Exception e) {
            log.error("Failed to process user event for email: {}", eventDto.getEmail(), e);
        }
    }
}