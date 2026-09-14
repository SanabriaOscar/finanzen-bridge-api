package com.findexso.bridge.shared.constants;

/** Mismos tipos que cloud {@code WebSocketEventTypes} para interoperabilidad Angular. */
public final class BridgeEventTypes {

    private BridgeEventTypes() {
    }

    public static final String BRIDGE_READY = "BRIDGE_READY";
    public static final String DEVICE_CONNECTED = "DEVICE_CONNECTED";
    public static final String DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED";
    public static final String WEIGHT_CHANGED = "WEIGHT_CHANGED";
    public static final String SCANNER_INPUT = "SCANNER_INPUT";
    public static final String PRINT_STATUS = "PRINT_STATUS";
    public static final String ERROR = "ERROR";
    public static final String HEARTBEAT = "HEARTBEAT";
    public static final String PRINT_TICKET = "PRINT_TICKET";
    public static final String OPEN_CASH_DRAWER = "OPEN_CASH_DRAWER";
    public static final String REQUEST_WEIGHT = "REQUEST_WEIGHT";
    public static final String TEST_PRINTER = "TEST_PRINTER";
    public static final String RESTART_DEVICE = "RESTART_DEVICE";
    public static final String SET_PRINTER_CONFIG = "SET_PRINTER_CONFIG";
    public static final String PING = "PING";
    public static final String GET_STATUS = "GET_STATUS";
}
