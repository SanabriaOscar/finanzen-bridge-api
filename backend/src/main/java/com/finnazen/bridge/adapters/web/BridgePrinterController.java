package com.finnazen.bridge.adapters.web;

import com.finnazen.bridge.application.port.out.PrinterPort;
import com.finnazen.bridge.infrastructure.hardware.EscPosPrinterAdapter;
import com.finnazen.bridge.shared.constants.BridgeConstants;
import com.finnazen.bridge.shared.constants.ThermalPrintConstants;
import com.finnazen.bridge.shared.response.BridgeResponse;
import com.finnazen.bridge.shared.response.BridgeResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(BridgeConstants.REST_BASE_PATH + "/printer")
public class BridgePrinterController {

    private final PrinterPort printerPort;

    public BridgePrinterController(PrinterPort printerPort) {
        this.printerPort = printerPort;
    }

    @GetMapping("/list")
    public ResponseEntity<BridgeResponse<Map<String, Object>>> listPrinters() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("available", printerPort.isAvailable());
        if (printerPort instanceof EscPosPrinterAdapter escPos) {
            data.put("resolved", escPos.resolvedPrinterName());
            data.put("printers", escPos.listPrinterNames());
            data.put("hint", escPos.installationHint());
        } else {
            data.put("resolved", null);
            data.put("printers", List.of());
            data.put("note", "Impresora deshabilitada (modo stub). Active finnazen.bridge.printer.enabled=true");
        }
        return ResponseEntity.ok(BridgeResponseFactory.ok("Impresoras detectadas", data));
    }

    @PostMapping("/test")
    public ResponseEntity<BridgeResponse<Map<String, Object>>> testPrint() {
        printerPort.printTestPage();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "TEST_OK");
        data.put("message", ThermalPrintConstants.MSG_TEST_PRINT_SENT);
        if (printerPort instanceof EscPosPrinterAdapter escPos) {
            data.put("printer", escPos.resolvedPrinterName());
        }
        return ResponseEntity.ok(BridgeResponseFactory.ok(BridgeConstants.MSG_PRINT_OK, data));
    }
}
