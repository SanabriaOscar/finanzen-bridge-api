package com.finnazen.bridge.infrastructure.hardware;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Generador mínimo de bytes ESC/POS para XPrinter POS (80 mm, fuente A ≈ 48 cols).
 */
public final class EscPosEncoder {

    /** Caracteres por línea en rollo 80 mm (fuente ESC/POS normal 12×24). */
    public static final int COLS_80MM = 48;
    /** Caracteres por línea en rollo 58 mm. */
    public static final int COLS_58MM = 32;
    /** Caracteres por línea en rollo 50 mm (XP-58 estrecho). */
    public static final int COLS_50MM = 24;

    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private EscPosEncoder() {
        init();
    }

    public static EscPosEncoder create() {
        return new EscPosEncoder();
    }

    public EscPosEncoder init() {
        write(new byte[]{0x1B, 0x40});
        return this;
    }

    public EscPosEncoder alignCenter() {
        write(new byte[]{0x1B, 0x61, 0x01});
        return this;
    }

    public EscPosEncoder alignLeft() {
        write(new byte[]{0x1B, 0x61, 0x00});
        return this;
    }

    public EscPosEncoder bold(boolean on) {
        write(new byte[]{0x1B, 0x45, (byte) (on ? 1 : 0)});
        return this;
    }

    public EscPosEncoder line(String text) {
        write(text != null ? text.getBytes(CHARSET) : new byte[0]);
        write(new byte[]{0x0A});
        return this;
    }

    public EscPosEncoder separator(int columns) {
        return line("-".repeat(Math.max(8, columns)));
    }

    public static int columnsForPaperWidth(int paperWidthMm) {
        if (paperWidthMm >= 80) {
            return COLS_80MM;
        }
        if (paperWidthMm <= 50) {
            return COLS_50MM;
        }
        return COLS_58MM;
    }

    public EscPosEncoder blankLines(int count) {
        for (int i = 0; i < count; i++) {
            write(new byte[]{0x0A});
        }
        return this;
    }

    public EscPosEncoder cutPartial() {
        write(new byte[]{0x1D, 0x56, 0x01});
        return this;
    }

    public EscPosEncoder openCashDrawer() {
        write(new byte[]{0x1B, 0x70, 0x00, 0x19, (byte) 0xFA});
        return this;
    }

    public byte[] toBytes() {
        return out.toByteArray();
    }

    private void write(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException ex) {
            throw new IllegalStateException("Error generando ESC/POS", ex);
        }
    }
}
