package com.finnazen.bridge.application.port.out;

import com.finnazen.bridge.domain.model.BridgeEnvelope;

public interface BridgeEventPublisherPort {

    void publishToAll(BridgeEnvelope envelope);

    void publishToSession(String sessionId, BridgeEnvelope envelope);

    int activeClients();
}
