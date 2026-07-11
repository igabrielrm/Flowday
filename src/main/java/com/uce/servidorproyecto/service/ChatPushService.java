package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.api.dto.ChatMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChatPushService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void pushToUser(Long userId, ChatMessageDto payload) {
        if (userId == null || payload == null) return;
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/chat",
                payload
        );
    }

    public void pushTyping(Long userId, Long fromUserId, boolean typing) {
        if (userId == null || fromUserId == null) return;
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/chat-typing",
                Map.of("fromUserId", fromUserId, "typing", typing)
        );
    }
}
