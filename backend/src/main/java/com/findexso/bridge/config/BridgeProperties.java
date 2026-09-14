package com.findexso.bridge.config;

import com.findexso.bridge.application.support.PosPrinterWidthSupport;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "findexso.bridge")
public record BridgeProperties(
        String host,
        int port,
        String wsPath,
        String pairingToken,
        boolean openBrowserOnStart,
        ScaleProperties scale,
        PrinterProperties printer
) {
  public record ScaleProperties(boolean enabled, String portName, double mockWeightKg, int pollIntervalMs) {
        public ScaleProperties {
            if (pollIntervalMs <= 0) {
                pollIntervalMs = 500;
            }
        }
    }

    /**
     * Impresora térmica POS ESC/POS (ej. XPrinter 80 mm).
     */
    public record PrinterProperties(boolean enabled, String name, int paperWidthMm) {
        public PrinterProperties {
            if (paperWidthMm <= 0) {
                paperWidthMm = PosPrinterWidthSupport.DEFAULT_MM;
            }
        }
    }
}
