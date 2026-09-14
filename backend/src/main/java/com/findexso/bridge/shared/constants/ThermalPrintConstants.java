package com.findexso.bridge.shared.constants;

/**
 * Constantes de impresión térmica ESC/POS.
 */
public final class ThermalPrintConstants {

    private ThermalPrintConstants() {
    }

    public static final String ALIGN_LEFT = "LEFT";
    public static final String ALIGN_CENTER = "CENTER";
    public static final String XPRINTER_NAME = "xprinter";
    public static final String XPRINTER_MODEL_PREFIX = "xp-";

    /** Marca en pie de ticket térmico (debe coincidir con API REST). */
    public static final String FOOTER_SOFTWARE = "Findexso POS";
    public static final String DEFAULT_BUSINESS_NAME = "FINDEXSO";
    public static final String TEST_PAGE_TITLE = "FINDEXSO";
    public static final String TEST_PAGE_SUBTITLE = "Prueba de impresora";
    public static final String MSG_TEST_PRINT_SENT = "Prueba Findexso enviada a la impresora";
}
