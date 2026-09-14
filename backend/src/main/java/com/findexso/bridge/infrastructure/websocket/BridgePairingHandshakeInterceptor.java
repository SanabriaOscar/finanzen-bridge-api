package com.findexso.bridge.infrastructure.websocket;

import com.findexso.bridge.config.BridgeProperties;
import com.findexso.bridge.shared.constants.BridgeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class BridgePairingHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(BridgePairingHandshakeInterceptor.class);

    private final BridgeProperties properties;

    public BridgePairingHandshakeInterceptor(BridgeProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        String token = servletRequest.getServletRequest().getParameter(BridgeConstants.WS_TOKEN_PARAM);
        boolean ok = properties.pairingToken() != null && properties.pairingToken().equals(token);
        if (!ok) {
            log.warn("Handshake bridge rechazado");
        }
        return ok;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
