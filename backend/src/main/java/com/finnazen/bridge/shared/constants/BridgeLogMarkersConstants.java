package com.finnazen.bridge.shared.constants;

/**
 * Marcadores grep-friendly para logs del Finnazen Bridge local.
 */
public final class BridgeLogMarkersConstants {

    public static final String BRIDGE = ">>> FINNANZEN_BRIDGE_ERROR <<<";
    public static final String HARDWARE = ">>> FINNANZEN_BRIDGE_HW_ERROR <<<";
    public static final String WEBSOCKET = ">>> FINNANZEN_BRIDGE_WS_ERROR <<<";

    public static final String GREP_BRIDGE = "FINNANZEN_BRIDGE_ERROR";
    public static final String GREP_HARDWARE = "FINNANZEN_BRIDGE_HW_ERROR";
    public static final String GREP_WEBSOCKET = "FINNANZEN_BRIDGE_WS_ERROR";

    private BridgeLogMarkersConstants() {
    }
}
