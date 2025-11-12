package com.cts.driver.kafka;

import com.cts.driver.dto.kafka.RideRequestEventDto;
import com.cts.driver.service.MatchingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RideRequestConsumer {

    @Autowired
    private MatchingService matchingService;

    @KafkaListener(
        topics = "${app.kafka.topics.ride-requests}", 
        containerFactory = "rideRequestListenerContainerFactory") // Uses the reliable factory
    public void handleRideRequest(RideRequestEventDto eventDto, Acknowledgment ack) {
        log.info("Received ride request event for booking ID: {}", eventDto.getBookingId());

        try {
            if (eventDto == null) {
                log.error("Received null RideRequestEventDto. This is a deserialization error.");
                ack.acknowledge();
                return;
            }

            matchingService.findDriversAndCreateOffers(eventDto);
            
            // Acknowledge the message
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing ride request for booking ID: {}", eventDto.getBookingId(), e);
            throw e;
        }
    }
}