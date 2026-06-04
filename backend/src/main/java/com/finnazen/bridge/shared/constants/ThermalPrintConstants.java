package com.finnazen.bridge.shared.constants;

/**
 * Constantes de impresión térmica ESC/POS.
 */
public final class ThermalPrintConstants {

    private ThermalPrintConstants() {
    }

    public static final String ALIGN_LEFT = "LEFT";
    public static final String ALIGN_CENTER = "CENTER";

    /** Marca en pie de ticket térmico (debe coincidir con API REST). */
    public static final String FOOTER_SOFTWARE = "Finanzen POS";
    public static final String DEFAULT_BUSINESS_NAME = "FINANZEN";
    public static final String TEST_PAGE_TITLE = "FINANZEN";
    public static final String TEST_PAGE_SUBTITLE = "Prueba de impresora";
    public static final String MSG_TEST_PRINT_SENT = "Prueba Finanzen enviada a la impresora";
}
