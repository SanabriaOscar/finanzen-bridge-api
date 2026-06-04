package com.finnazen.bridge.application.support;

public final class PosPrinterWidthSupport {

    public static final int WIDTH_50_MM = 50;
    public static final int WIDTH_58_MM = 58;
    public static final int WIDTH_80_MM = 80;
    public static final int DEFAULT_MM = WIDTH_58_MM;

    private PosPrinterWidthSupport() {
    }

    public static int normalize(int mm) {
        if (mm == WIDTH_80_MM) {
            return WIDTH_80_MM;
        }
        if (mm == WIDTH_58_MM) {
            return WIDTH_58_MM;
        }
        if (mm == WIDTH_50_MM) {
            return WIDTH_50_MM;
        }
        return DEFAULT_MM;
    }
}
