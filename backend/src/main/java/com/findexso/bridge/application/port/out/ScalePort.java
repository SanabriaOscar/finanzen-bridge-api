package com.findexso.bridge.application.port.out;

import com.findexso.bridge.domain.model.WeightReading;

public interface ScalePort {

    boolean isAvailable();

    WeightReading readWeight();
}
