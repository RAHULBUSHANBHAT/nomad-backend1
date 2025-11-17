package com.cts.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.cts.dto.kafka.UserEventDto;
import com.cts.model.Status;
import com.cts.model.UserCredential;
import com.cts.repository.UserCredentialRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserEventConsumer {

    @Autowired
    private UserCredentialRepository repository;

    @KafkaListener(
        topics = "${app.kafka.topics.user-events}", 
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory")
    public void handleUserEvent(UserEventDto eventDto) {
        log.info("Received Kafka message: eventType={}, userId={}, email={}", 
                eventDto.getEventType(), eventDto.getUserId(), eventDto.getEmail());

        Status newStatus;
        try {
            if (eventDto.getStatus() != null) {
                newStatus = Status.valueOf(eventDto.getStatus().toUpperCase());
            } else {
                newStatus = null;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid status string received in event: '{}'. Skipping message.", eventDto.getStatus());
            return;
        }
        
        if ("USER_CREATED".equals(eventDto.getEventType())) {
            log.debug("Processing USER_CREATED event for user: {}", eventDto.getEmail());
            
            if(repository.existsById(eventDto.getUserId())) {
                log.warn("UserCredential for user {} already exists. Skipping creation.", eventDto.getUserId());
                return;
            }

            if (newStatus == null) {
                log.error("USER_CREATED event for user {} has a null status. Skipping.", eventDto.getUserId());
                return;
            }

            log.debug("Creating new UserCredential with userId={}, email={}, role={}", 
                    eventDto.getUserId(), eventDto.getEmail(), eventDto.getRole());

            UserCredential credential = new UserCredential(
                    eventDto.getUserId(),
                    eventDto.getEmail(),
                    eventDto.getPassword(),
                    eventDto.getRole(),
                    newStatus
            );

            try {
                UserCredential saved = repository.save(credential);
                log.info("Successfully created UserCredential. ID: {}, Email: {}", saved.getUserId(), saved.getEmail());
            } catch (Exception e) {
                log.error("Database error saving UserCredential: {}", e.getMessage(), e);
                throw e;
            }

        } else if ("USER_STATUS_UPDATED".equals(eventDto.getEventType())) {
            log.debug("Processing USER_STATUS_UPDATED event for user: {}", eventDto.getUserId());
            
            if (newStatus == null) {
                log.error("USER_STATUS_UPDATED event for user {} has a null status. Skipping.", eventDto.getUserId());
                return;
            }

            UserCredential existingCredential = repository.findById(eventDto.getUserId())
                .orElse(null);

            if (existingCredential == null) {
                log.warn("UserCredential for user {} does not exist. Skipping update.", eventDto.getUserId());
                return;
            }

            log.debug("Updating status for userId={} from {} to {}", 
                    eventDto.getUserId(), existingCredential.getStatus(), newStatus);
            
            existingCredential.setStatus(newStatus);
            
            try {
                repository.save(existingCredential);
                log.info("Successfully updated status for UserCredential. ID: {}", existingCredential.getUserId());
            } catch (Exception e) {
                log.error("Database error updating UserCredential: {}", e.getMessage(), e);
                throw e;
            }
        }
    }
}