package com.finnazen.bridge.infrastructure.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BridgeSessionRegistry {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    void add(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    void remove(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    Collection<WebSocketSession> all() {
        return sessions.values();
    }

    Optional<WebSocketSession> find(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    int size() {
        return sessions.size();
    }
}
