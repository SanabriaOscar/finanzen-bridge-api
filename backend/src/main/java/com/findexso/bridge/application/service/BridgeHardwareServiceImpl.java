package com.findexso.bridge.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.findexso.bridge.application.port.in.IBridgeHardwareService;
import com.findexso.bridge.application.port.out.BridgeEventPublisherPort;
import com.findexso.bridge.application.port.out.PrinterPort;
import com.findexso.bridge.application.port.out.ScalePort;
import com.findexso.bridge.config.BridgeProperties;
import com.findexso.bridge.config.BridgeRuntimePrinterConfig;
import com.findexso.bridge.domain.model.BridgeEnvelope;
import com.findexso.bridge.domain.model.PrintTicketCommand;
import com.findexso.bridge.domain.model.WeightReading;
import com.findexso.bridge.shared.constants.BridgeConstants;
import com.findexso.bridge.shared.constants.BridgeEventTypes;
import com.findexso.bridge.shared.constants.ThermalPrintConstants;
import com.findexso.bridge.application.support.PosPrinterWidthSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BridgeHardwareServiceImpl implements IBridgeHardwareService {

    private static final Logger log = LoggerFactory.getLogger(BridgeHardwareServiceImpl.class);
    private static final String UNIT_KG = "KG";

    private final ScalePort scalePort;
    private final PrinterPort printerPort;
    private final BridgeEventPublisherPort publisher;
    private final BridgeProperties properties;
    private final ObjectMapper objectMapper;
    private final BridgeRuntimePrinterConfig runtimePrinterConfig;
    private double lastWeight = -1;

    public BridgeHardwareServiceImpl(ScalePort scalePort,
                                     PrinterPort printerPort,
                                     BridgeEventPublisherPort publisher,
                                     BridgeProperties properties,
                                     ObjectMapper objectMapper,
                                     BridgeRuntimePrinterConfig runtimePrinterConfig) {
        this.scalePort = scalePort;
        this.printerPort = printerPort;
        this.publisher = publisher;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.runtimePrinterConfig = runtimePrinterConfig;
    }

    @Override
    public BridgeEnvelope handleIncomingCommand(String sessionId, JsonNode root) {
        String type = root.path("type").asText(BridgeEventTypes.PING);
        return switch (type) {
            case BridgeEventTypes.PING, BridgeEventTypes.GET_STATUS -> getStatusEnvelope();
            case BridgeEventTypes.REQUEST_WEIGHT -> weightEnvelope(readWeightSafe());
            case BridgeEventTypes.PRINT_TICKET -> printTicket(root.path("payload"));
            case BridgeEventTypes.TEST_PRINTER -> testPrinter();
            case BridgeEventTypes.OPEN_CASH_DRAWER -> openDrawer();
            case BridgeEventTypes.SET_PRINTER_CONFIG -> setPrinterConfig(root.path("payload"));
            case BridgeEventTypes.SCANNER_INPUT -> relayScannerInput(root.path("payload"));
            default -> errorEnvelope("Comando no soportado: " + type);
        };
    }

    @Override
    public BridgeEnvelope getStatusEnvelope() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("app", BridgeConstants.APP_NAME);
        payload.put("version", "1.0.0");
        payload.put("clients", publisher.activeClients());
        payload.put("scaleAvailable", scalePort.isAvailable());
        payload.put("printerAvailable", printerPort.isAvailable());
        payload.put("paperWidthMm", runtimePrinterConfig.getPaperWidthMm());
        payload.put("host", properties.host());
        payload.put("port", properties.port());
        return envelope(BridgeEventTypes.BRIDGE_READY, payload);
    }

    @Override
    public void broadcastWeightIfChanged() {
        WeightReading reading = readWeightSafe();
        if (Math.abs(reading.value() - lastWeight) < 0.001) {
            return;
        }
        lastWeight = reading.value();
        publisher.publishToAll(weightEnvelope(reading));
    }

    private BridgeEnvelope printTicket(JsonNode payload) {
        try {
            PrintTicketCommand command = parsePrintCommand(payload);
            int lineCount = command.printLines() != null ? command.printLines().size() : 0;
            log.info("PRINT_TICKET ticketId={} printLines={}", command.ticketId(), lineCount);
            printerPort.printTicket(command);
            ObjectNode ok = objectMapper.createObjectNode();
            ok.put("ticketId", command.ticketId());
            ok.put("status", "OK");
            ok.put("printLines", lineCount);
            return envelope(BridgeEventTypes.PRINT_STATUS, ok);
        } catch (Exception ex) {
            log.warn("Error imprimiendo ticket: {}", ex.getMessage());
            return errorEnvelope(BridgeConstants.MSG_PRINT_FAIL);
        }
    }

    private PrintTicketCommand parsePrintCommand(JsonNode payload) throws com.fasterxml.jackson.core.JsonProcessingException {
        PrintTicketCommand base = objectMapper.treeToValue(payload, PrintTicketCommand.class);
        List<PrintTicketCommand.FormattedPrintLine> lines = parsePrintLines(payload.path("printLines"));
        if (lines.isEmpty()) {
            return base;
        }
        return new PrintTicketCommand(
                base.ticketId(),
                base.items(),
                base.total(),
                base.tenantId(),
                base.businessName(),
                base.customerName(),
                base.payMethodName(),
                base.sellerName(),
                base.dateLabel(),
                base.paperWidthMm(),
                base.totalBase(),
                base.totalTax(),
                base.publicSaleId(),
                lines
        );
    }

    private List<PrintTicketCommand.FormattedPrintLine> parsePrintLines(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<PrintTicketCommand.FormattedPrintLine> out = new ArrayList<>();
        for (JsonNode row : node) {
            if (row == null || row.isNull()) {
                continue;
            }
            out.add(new PrintTicketCommand.FormattedPrintLine(
                    row.path("text").asText(""),
                    row.path("align").asText(ThermalPrintConstants.ALIGN_LEFT),
                    row.path("bold").asBoolean(false)
            ));
        }
        return out;
    }

    private BridgeEnvelope testPrinter() {
        try {
            printerPort.printTestPage();
            ObjectNode ok = objectMapper.createObjectNode();
            ok.put("status", "TEST_OK");
            return envelope(BridgeEventTypes.PRINT_STATUS, ok);
        } catch (Exception ex) {
            return errorEnvelope(ex.getMessage());
        }
    }

    private BridgeEnvelope setPrinterConfig(JsonNode payload) {
        int mm = payload.path("paperWidthMm").asInt(runtimePrinterConfig.getPaperWidthMm());
        runtimePrinterConfig.setPaperWidthMm(PosPrinterWidthSupport.normalize(mm));
        ObjectNode ok = objectMapper.createObjectNode();
        ok.put("paperWidthMm", runtimePrinterConfig.getPaperWidthMm());
        ok.put("status", "OK");
        return envelope(BridgeEventTypes.PRINT_STATUS, ok);
    }

    private BridgeEnvelope openDrawer() {
        try {
            printerPort.openCashDrawer();
            ObjectNode ok = objectMapper.createObjectNode();
            ok.put("status", "DRAWER_OPEN");
            return envelope(BridgeEventTypes.PRINT_STATUS, ok);
        } catch (Exception ex) {
            return errorEnvelope(ex.getMessage());
        }
    }

    private WeightReading readWeightSafe() {
        if (!scalePort.isAvailable()) {
            return new WeightReading(0, UNIT_KG);
        }
        return scalePort.readWeight();
    }

    private BridgeEnvelope weightEnvelope(WeightReading reading) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("value", reading.value());
        payload.put("unit", reading.unit());
        return envelope(BridgeEventTypes.WEIGHT_CHANGED, payload);
    }

    private BridgeEnvelope relayScannerInput(JsonNode payload) {
        String code = payload.path(BridgeConstants.PAYLOAD_CODE).asText("").trim();
        if (code.isEmpty()) {
            return errorEnvelope(BridgeConstants.MSG_SCANNER_CODE_REQUIRED);
        }
        ObjectNode out = objectMapper.createObjectNode();
        out.put(BridgeConstants.PAYLOAD_CODE, code);
        out.put(BridgeConstants.PAYLOAD_SOURCE, payload.path(BridgeConstants.PAYLOAD_SOURCE).asText(BridgeConstants.SCANNER_SOURCE_HID));
        BridgeEnvelope broadcast = envelope(BridgeEventTypes.SCANNER_INPUT, out);
        publisher.publishToAll(broadcast);
        log.info("SCANNER_INPUT reenviado a clientes code={}", code);
        return broadcast;
    }

    private BridgeEnvelope errorEnvelope(String message) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("message", message);
        return envelope(BridgeEventTypes.ERROR, payload);
    }

    private BridgeEnvelope envelope(String type, ObjectNode payload) {
        return new BridgeEnvelope(BridgeConstants.SCHEMA_VERSION, type, payload, System.currentTimeMillis());
    }
}
