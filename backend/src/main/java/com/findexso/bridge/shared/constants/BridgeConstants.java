package com.findexso.bridge.shared.constants;

public final class BridgeConstants {

    private BridgeConstants() {
    }

    public static final String REST_BASE_PATH = "/api/bridge";
    public static final String WS_PATH = "/ws";
    public static final String WS_TOKEN_PARAM = "token";
    public static final int SCHEMA_VERSION = 1;
    public static final String APP_NAME = "Findexso Bridge";
    public static final String EXE_NAME = "findexso-bridge.exe";

    public static final String MSG_READY = "Bridge listo";
    public static final String MSG_UNAUTHORIZED = "Token de emparejamiento inválido";
    public static final String MSG_PRINT_OK = "Impresión enviada";
    public static final String MSG_PRINT_FAIL = "No se pudo imprimir";
    public static final String MSG_STATUS_OK = "Estado del bridge";
    public static final String MSG_SCANNER_CODE_REQUIRED = "Código de escaneo vacío";

    public static final String PAYLOAD_CODE = "code";
    public static final String PAYLOAD_SOURCE = "source";
    public static final String SCANNER_SOURCE_HID = "HID";
}
