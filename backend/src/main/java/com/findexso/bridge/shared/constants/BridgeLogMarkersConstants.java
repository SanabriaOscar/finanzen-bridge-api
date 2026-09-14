package com.findexso.bridge.shared.constants;

/**
 * Marcadores grep-friendly para logs del Findexso Bridge local.
 */
public final class BridgeLogMarkersConstants {

    public static final String BRIDGE = ">>> FINDEXSO_BRIDGE_ERROR <<<";
    public static final String HARDWARE = ">>> FINDEXSO_BRIDGE_HW_ERROR <<<";
    public static final String WEBSOCKET = ">>> FINDEXSO_BRIDGE_WS_ERROR <<<";

    public static final String GREP_BRIDGE = "FINDEXSO_BRIDGE_ERROR";
    public static final String GREP_HARDWARE = "FINDEXSO_BRIDGE_HW_ERROR";
    public static final String GREP_WEBSOCKET = "FINDEXSO_BRIDGE_WS_ERROR";

    private BridgeLogMarkersConstants() {
    }
}
