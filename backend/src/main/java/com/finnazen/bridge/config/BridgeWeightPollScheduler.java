package com.finnazen.bridge.config;

import com.finnazen.bridge.application.port.in.IBridgeHardwareService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Publica {@code WEIGHT_CHANGED} por WebSocket cuando el peso en báscula cambia.
 */
@Component
@ConditionalOnProperty(name = "finnazen.bridge.scale.enabled", havingValue = "true")
public class BridgeWeightPollScheduler {

    private final IBridgeHardwareService hardwareService;

    public BridgeWeightPollScheduler(IBridgeHardwareService hardwareService) {
        this.hardwareService = hardwareService;
    }

    @Scheduled(fixedDelayString = "${finnazen.bridge.scale.poll-interval-ms:500}")
    public void pollScale() {
        hardwareService.broadcastWeightIfChanged();
    }
}
