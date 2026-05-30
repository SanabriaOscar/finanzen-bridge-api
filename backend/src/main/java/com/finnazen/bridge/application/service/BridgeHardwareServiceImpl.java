package com.finnazen.bridge.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finnazen.bridge.application.port.in.IBridgeHardwareService;
import com.finnazen.bridge.application.port.out.BridgeEventPublisherPort;
import com.finnazen.bridge.application.port.out.PrinterPort;
import com.finnazen.bridge.application.port.out.ScalePort;
import com.finnazen.bridge.config.BridgeProperties;
import com.finnazen.bridge.domain.model.BridgeEnvelope;
import com.finnazen.bridge.domain.model.PrintTicketCommand;
import com.finnazen.bridge.domain.model.WeightReading;
import com.finnazen.bridge.shared.constants.BridgeConstants;
import com.finnazen.bridge.shared.constants.BridgeEventTypes;
import com.finnazen.bridge.shared.constants.ThermalPrintConstants;
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
    private double lastWeight = -1;

    public BridgeHardwareServiceImpl(ScalePort scalePort,
                                     PrinterPort printerPort,
                                     BridgeEventPublisherPort publisher,
                                     BridgeProperties properties,
                                     ObjectMapper objectMapper) {
        this.scalePort = scalePort;
        this.printerPort = printerPort;
        this.publisher = publisher;
        this.properties = properties;
        this.objectMapper = objectMapper;
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

    private BridgeEnvelope errorEnvelope(String message) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("message", message);
        return envelope(BridgeEventTypes.ERROR, payload);
    }

    private BridgeEnvelope envelope(String type, ObjectNode payload) {
        return new BridgeEnvelope(BridgeConstants.SCHEMA_VERSION, type, payload, System.currentTimeMillis());
    }
}
