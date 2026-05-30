package com.finnazen.bridge.infrastructure.hardware;

import com.finnazen.bridge.application.port.out.ScalePort;
import com.finnazen.bridge.config.BridgeProperties;
import com.finnazen.bridge.domain.model.WeightReading;
import org.springframework.stereotype.Component;

@Component
public class StubScaleAdapter implements ScalePort {

    private static final String UNIT_KG = "KG";
    private final BridgeProperties properties;

    public StubScaleAdapter(BridgeProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isAvailable() {
        return properties.scale().enabled();
    }

    @Override
    public WeightReading readWeight() {
        return new WeightReading(0, UNIT_KG);
    }
}
