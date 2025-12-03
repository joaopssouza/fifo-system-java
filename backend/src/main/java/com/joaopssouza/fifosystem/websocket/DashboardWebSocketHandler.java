package com.joaopssouza.fifosystem.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joaopssouza.fifosystem.domain.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    // Injetamos apenas o ObjectMapper (padrão do Spring)
    public DashboardWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        // Apenas avisa quem está online ao conectar
        broadcastOnlineUsers();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        // Atualiza a lista ao desconectar
        broadcastOnlineUsers();
    }

    private void broadcastOnlineUsers() {
        Map<Long, Map<String, Object>> uniqueUsers = new HashMap<>();
        
        for (WebSocketSession session : sessions) {
            User user = (User) session.getAttributes().get("user");
            if (user != null) {
                uniqueUsers.put(user.getId(), Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "fullName", user.getFullName(),
                    "role", user.getRole().getName(),
                    "sector", user.getSector()
                ));
            }
        }

        Map<String, Object> message = Map.of(
            "type", "online_users",
            "data", new ArrayList<>(uniqueUsers.values())
        );

        try {
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                User user = (User) session.getAttributes().get("user");
                // Apenas Admins e Líderes recebem esta lista
                if (user != null && (user.getRole().getName().equals("admin") || user.getRole().getName().equals("leader"))) {
                     if (session.isOpen()) session.sendMessage(textMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}