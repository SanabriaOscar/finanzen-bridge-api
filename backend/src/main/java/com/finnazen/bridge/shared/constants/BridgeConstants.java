package com.finnazen.bridge.shared.constants;

public final class BridgeConstants {

    private BridgeConstants() {
    }

    public static final String REST_BASE_PATH = "/api/bridge";
    public static final String WS_PATH = "/ws";
    public static final String WS_TOKEN_PARAM = "token";
    public static final int SCHEMA_VERSION = 1;
    public static final String APP_NAME = "Finnazen Bridge";
    public static final String EXE_NAME = "finanzen-bridge.exe";

    public static final String MSG_READY = "Bridge listo";
    public static final String MSG_UNAUTHORIZED = "Token de emparejamiento inválido";
    public static final String MSG_PRINT_OK = "Impresión enviada";
    public static final String MSG_PRINT_FAIL = "No se pudo imprimir";
    public static final String MSG_STATUS_OK = "Estado del bridge";
}
