package com.cts.kafka;

import com.cts.dto.kafka.UserEventDto;
import com.cts.model.UserCredential;
import com.cts.repository.UserCredentialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    @Autowired
    private UserCredentialRepository repository;

    /**
     * Listens for messages on the 'user-events-topic'.
     * The groupId is a property in application.yml
     */
    @KafkaListener(topics = "${app.kafka.topics.user-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserEvent(UserEventDto eventDto) {
        log.info("Received user event from Kafka: EventType={}, UserEmail={}", 
            eventDto.getEventType(), eventDto.getEmail());

        try {
            // We only care about the "USER_CREATED" event
            if ("USER_CREATED".equals(eventDto.getEventType())) {
                
                // Idempotency check: Do we already have this user?
                if(repository.existsById(eventDto.getUserId())) {
                    log.warn("UserCredential for user {} already exists. Skipping creation.", eventDto.getUserId());
                    return;
                }

                // Create a new UserCredential entity from the event
                UserCredential credential = new UserCredential(
                        eventDto.getUserId(),
                        eventDto.getEmail(),
                        eventDto.getPassword(), // Password is *already* hashed by user-service
                        eventDto.getRole()
                );

                // Save to this service's local database
                repository.save(credential);
                log.info("Successfully created local UserCredential for: {}", credential.getEmail());
            } 
            // We could add else-if blocks here for "USER_PASSWORD_UPDATED" or "USER_DELETED"
            
        } catch (Exception e) {
            log.error("Failed to process user event for email: {}", eventDto.getEmail(), e);
            // In a production system, this message would go to a Dead Letter Queue (DLQ)
        }
    }
}