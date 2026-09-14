package com.findexso.bridge.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.findexso.bridge.domain.model.BridgeEnvelope;

public interface IBridgeHardwareService {

    BridgeEnvelope handleIncomingCommand(String sessionId, JsonNode root);

    BridgeEnvelope getStatusEnvelope();

    void broadcastWeightIfChanged();
}
