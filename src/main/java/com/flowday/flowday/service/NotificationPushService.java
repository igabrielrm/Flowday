package com.flowday.flowday.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationPushService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void pushToUser(Long userId, Map<String, Object> payload) {
        if (userId == null || payload == null) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                payload
        );
    }
}
