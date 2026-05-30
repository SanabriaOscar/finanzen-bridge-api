package com.finnazen.bridge.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finnazen.bridge.application.port.out.BridgeEventPublisherPort;
import com.finnazen.bridge.domain.model.BridgeEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class BridgeWebSocketPublisher implements BridgeEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(BridgeWebSocketPublisher.class);

    private final BridgeSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public BridgeWebSocketPublisher(BridgeSessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishToAll(BridgeEnvelope envelope) {
        sessionRegistry.all().forEach(session -> sendSafe(session, envelope));
    }

    @Override
    public void publishToSession(String sessionId, BridgeEnvelope envelope) {
        sessionRegistry.find(sessionId).ifPresent(session -> sendSafe(session, envelope));
    }

    @Override
    public int activeClients() {
        return sessionRegistry.size();
    }

    private void sendSafe(WebSocketSession session, BridgeEnvelope envelope) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
        } catch (Exception ex) {
            log.debug("Publish error session={}: {}", session.getId(), ex.getMessage());
        }
    }
}
