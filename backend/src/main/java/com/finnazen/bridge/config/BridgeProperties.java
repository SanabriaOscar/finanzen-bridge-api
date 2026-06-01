package com.finnazen.bridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "finnazen.bridge")
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
                paperWidthMm = 80;
            }
        }
    }
}
