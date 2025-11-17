package com.cts.wallet.kafka;

import com.cts.wallet.dto.kafka.UserEventDto;
import com.cts.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;


@Component
public class UserEventConsumer {
    

    @Autowired
    private WalletService walletService;

    @KafkaListener(
        topics = "${app.kafka.topics.user-events}", 
        containerFactory = "kafkaListenerContainerFactory") // Uses the robust factory
    public void handleUserEvent(UserEventDto eventDto, Acknowledgment ack) {
        try {
            if (eventDto == null) {
                // This happens if the message was corrupted and skipped by the ErrorHandler
               
                ack.acknowledge(); // Acknowledge and skip the bad message
                return;
            }

           

            if ("USER_CREATED".equals(eventDto.getEventType())) {
                // Create a wallet for *every* new user (Rider, Driver, Admin)
                walletService.createWallet(eventDto.getUserId());
            } 
            
            ack.acknowledge(); // Acknowledge the message (commit the offset)

        } catch (Exception e) {
        
            // We do NOT acknowledge, so the DefaultErrorHandler will log it
            // and the app will not be stuck in a crash loop.
            // We re-throw to trigger the error handler.
            throw new RuntimeException("Failed to process user event, see logs for details", e);
        }
    }
}