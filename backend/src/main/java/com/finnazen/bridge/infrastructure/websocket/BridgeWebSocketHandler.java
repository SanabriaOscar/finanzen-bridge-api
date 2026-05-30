package com.finnazen.bridge.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finnazen.bridge.application.port.in.IBridgeHardwareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class BridgeWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BridgeWebSocketHandler.class);

    private final IBridgeHardwareService hardwareService;
    private final ObjectMapper objectMapper;
    private final BridgeSessionRegistry sessionRegistry;

    public BridgeWebSocketHandler(IBridgeHardwareService hardwareService,
                                  ObjectMapper objectMapper,
                                  BridgeSessionRegistry sessionRegistry) {
        this.hardwareService = hardwareService;
        this.objectMapper = objectMapper;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionRegistry.add(session);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(hardwareService.getStatusEnvelope())));
        log.info("Cliente bridge conectado id={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var root = objectMapper.readTree(message.getPayload());
        var response = hardwareService.handleIncomingCommand(session.getId(), root);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.remove(session);
    }
}
