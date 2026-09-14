package com.findexso.bridge.infrastructure.hardware;

import com.findexso.bridge.application.port.out.ScalePort;
import com.findexso.bridge.config.BridgeProperties;
import com.findexso.bridge.domain.model.WeightReading;
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
        double mockKg = properties.scale().mockWeightKg();
        if (mockKg > 0) {
            return new WeightReading(mockKg, UNIT_KG);
        }
        return new WeightReading(0, UNIT_KG);
    }
}
