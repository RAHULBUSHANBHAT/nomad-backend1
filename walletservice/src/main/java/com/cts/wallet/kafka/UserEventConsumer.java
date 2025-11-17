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
        containerFactory = "kafkaListenerContainerFactory")
    public void handleUserEvent(UserEventDto eventDto, Acknowledgment ack) {
        try {
            if (eventDto == null) {
                ack.acknowledge();
                return;
            }

           

            if ("USER_CREATED".equals(eventDto.getEventType())) {
                walletService.createWallet(eventDto.getUserId());
            } 
            
            ack.acknowledge();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process user event, see logs for details", e);
        }
    }
}