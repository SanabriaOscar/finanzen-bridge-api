package com.finnazen.bridge.config;

import com.finnazen.bridge.application.support.PosPrinterWidthSupport;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ancho de papel en runtime (configuración desde Angular vía WebSocket).
 */
@Component
public class BridgeRuntimePrinterConfig {

    private final AtomicInteger paperWidthMm;

    public BridgeRuntimePrinterConfig(BridgeProperties properties) {
        int initial = properties.printer() != null ? properties.printer().paperWidthMm() : PosPrinterWidthSupport.DEFAULT_MM;
        this.paperWidthMm = new AtomicInteger(PosPrinterWidthSupport.normalize(initial));
    }

    public int getPaperWidthMm() {
        return paperWidthMm.get();
    }

    public void setPaperWidthMm(int mm) {
        paperWidthMm.set(PosPrinterWidthSupport.normalize(mm));
    }
}
