package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.service.ChatPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatStompController {

    @Autowired
    private ChatPushService chatPushService;

    @MessageMapping("/chat/typing")
    public void typing(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null || payload == null) return;
        Object destId = payload.get("destinatarioId");
        if (destId == null) return;
        Long remitenteId = Long.parseLong(principal.getName());
        Long destinatarioId = Long.parseLong(String.valueOf(destId));
        boolean typing = Boolean.TRUE.equals(payload.get("typing"));
        chatPushService.pushTyping(destinatarioId, remitenteId, typing);
    }
}
