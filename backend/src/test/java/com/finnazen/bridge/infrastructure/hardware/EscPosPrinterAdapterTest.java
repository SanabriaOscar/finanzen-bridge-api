package com.finnazen.bridge.infrastructure.hardware;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EscPosPrinterAdapterTest {

    @Test
    @DisplayName("matches encuentra XPrinter por nombre parcial")
    void matches_xprinter() {
        assertThat(EscPosPrinterAdapter.matches("XP-80C", "XPrinter")).isTrue();
        assertThat(EscPosPrinterAdapter.matches("Generic PDF", "XPrinter")).isFalse();
    }

    @Test
    @DisplayName("columnsForPaperWidth usa 48 cols en rollo 80mm")
    void columns_80mm() {
        assertThat(EscPosEncoder.columnsForPaperWidth(80)).isEqualTo(EscPosEncoder.COLS_80MM);
        assertThat(EscPosEncoder.columnsForPaperWidth(58)).isEqualTo(EscPosEncoder.COLS_58MM);
    }

    @Test
    @DisplayName("EscPosEncoder genera bytes de corte")
    void encoder_includesCutCommand() {
        byte[] data = EscPosEncoder.create()
                .line("prueba finazen")
                .cutPartial()
                .toBytes();
        assertThat(data.length).isGreaterThan(4);
        assertThat(data[data.length - 3]).isEqualTo((byte) 0x1D);
    }
}
