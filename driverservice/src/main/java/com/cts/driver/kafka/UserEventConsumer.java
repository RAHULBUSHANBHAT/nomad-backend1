package com.cts.driver.kafka;

import com.cts.driver.config.KafkaConsumerConfig;
import com.cts.driver.dto.kafka.UserEventDto;
import com.cts.driver.model.Driver;
import com.cts.driver.repository.DriverRepository;
// import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

// @Slf4j
@Component
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Autowired
    private DriverRepository driverRepository;

    @KafkaListener(
        topics = "${app.kafka.topics.user-events}", 
        containerFactory = "userEventListenerContainerFactory") // Uses the reliable factory
    public void handleUserEvent(UserEventDto eventDto, Acknowledgment ack) {
        log.info("Received user event: {} for user ID: {}", eventDto.getEventType(), eventDto.getUserId());

        try {
            if (eventDto == null) {

                ack.acknowledge();
                return;
            }

            if ("USER_CREATED".equals(eventDto.getEventType()) && "DRIVER".equals(eventDto.getRole())) {
                
                if (driverRepository.findByUserId(eventDto.getUserId()).isPresent()) {
                    log.warn("Driver profile already exists for user ID: {}, skipping.", eventDto.getUserId());
                } else {
                    Driver newDriver = new Driver();
                    newDriver.setUserId(eventDto.getUserId());
                    newDriver.setAvailable(false); // Driver must verify before going online
                    newDriver.setCurrentCity("Unset"); // Default city until driver sets location
                    
                    driverRepository.save(newDriver);
                    log.info("Successfully created new driver profile for user ID: {}", eventDto.getUserId());
                }
            }
            
            // Acknowledge the message (commit the offset)
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing user event for user ID: {}", eventDto.getUserId(), e);
            // We do NOT acknowledge, so the error handler will catch it
            throw e; 
        }
    }
}