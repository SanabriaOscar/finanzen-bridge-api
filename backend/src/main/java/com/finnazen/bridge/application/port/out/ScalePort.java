package com.finnazen.bridge.application.port.out;

import com.finnazen.bridge.domain.model.WeightReading;

public interface ScalePort {

    boolean isAvailable();

    WeightReading readWeight();
}
