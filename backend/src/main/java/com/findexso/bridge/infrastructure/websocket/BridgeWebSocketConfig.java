package com.findexso.bridge.infrastructure.websocket;

import com.findexso.bridge.config.BridgeProperties;
import com.findexso.bridge.shared.constants.BridgeConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class BridgeWebSocketConfig implements WebSocketConfigurer {

    private final BridgeWebSocketHandler bridgeWebSocketHandler;
    private final BridgePairingHandshakeInterceptor pairingInterceptor;
    private final BridgeProperties properties;

    public BridgeWebSocketConfig(BridgeWebSocketHandler bridgeWebSocketHandler,
                                 BridgePairingHandshakeInterceptor pairingInterceptor,
                                 BridgeProperties properties) {
        this.bridgeWebSocketHandler = bridgeWebSocketHandler;
        this.pairingInterceptor = pairingInterceptor;
        this.properties = properties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(bridgeWebSocketHandler, BridgeConstants.WS_PATH)
                .addInterceptors(pairingInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
