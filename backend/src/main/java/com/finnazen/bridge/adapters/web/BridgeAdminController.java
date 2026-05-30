package com.finnazen.bridge.adapters.web;

import com.finnazen.bridge.application.port.in.IBridgeHardwareService;
import com.finnazen.bridge.application.port.out.BridgeEventPublisherPort;
import com.finnazen.bridge.config.BridgeProperties;
import com.finnazen.bridge.domain.model.BridgeEnvelope;
import com.finnazen.bridge.shared.constants.BridgeConstants;
import com.finnazen.bridge.shared.response.BridgeResponse;
import com.finnazen.bridge.shared.response.BridgeResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(BridgeConstants.REST_BASE_PATH)
public class BridgeAdminController {

    private final IBridgeHardwareService hardwareService;
    private final BridgeEventPublisherPort publisher;
    private final BridgeProperties properties;

    public BridgeAdminController(IBridgeHardwareService hardwareService,
                                 BridgeEventPublisherPort publisher,
                                 BridgeProperties properties) {
        this.hardwareService = hardwareService;
        this.publisher = publisher;
        this.properties = properties;
    }

    @GetMapping("/status")
    public ResponseEntity<BridgeResponse<Map<String, Object>>> status() {
        BridgeEnvelope envelope = hardwareService.getStatusEnvelope();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("schemaVersion", envelope.schemaVersion());
        data.put("type", envelope.type());
        data.put("payload", envelope.payload());
        data.put("clients", publisher.activeClients());
        data.put("adminUrl", "http://" + properties.host() + ":" + properties.port() + "/");
        data.put("wsUrl", "ws://" + properties.host() + ":" + properties.port() + BridgeConstants.WS_PATH);
        return ResponseEntity.ok(BridgeResponseFactory.ok(BridgeConstants.MSG_STATUS_OK, data));
    }
}
