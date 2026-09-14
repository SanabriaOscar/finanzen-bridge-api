package com.findexso.bridge.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.findexso.bridge.application.port.out.BridgeEventPublisherPort;
import com.findexso.bridge.application.port.out.PrinterPort;
import com.findexso.bridge.application.port.out.ScalePort;
import com.findexso.bridge.config.BridgeProperties;
import com.findexso.bridge.config.BridgeRuntimePrinterConfig;
import com.findexso.bridge.shared.constants.BridgeEventTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgeHardwareServiceImplTest {

    @Mock private ScalePort scalePort;
    @Mock private PrinterPort printerPort;
    @Mock private BridgeEventPublisherPort publisher;

    private BridgeHardwareServiceImpl service;

    @BeforeEach
    void setUp() {
        BridgeProperties props = new BridgeProperties(
                "127.0.0.1", 9095, "/ws", "token", false,
                new BridgeProperties.ScaleProperties(false, "COM1", 0, 500),
                new BridgeProperties.PrinterProperties(false, "default", 50)
        );
        BridgeRuntimePrinterConfig runtime = new BridgeRuntimePrinterConfig(props);
        service = new BridgeHardwareServiceImpl(scalePort, printerPort, publisher, props, new ObjectMapper(), runtime);
    }

    @Test
    @DisplayName("getStatusEnvelope incluye BRIDGE_READY")
    void statusEnvelope_typeReady() {
        when(publisher.activeClients()).thenReturn(0);
        when(scalePort.isAvailable()).thenReturn(false);
        when(printerPort.isAvailable()).thenReturn(false);
        assertThat(service.getStatusEnvelope().type()).isEqualTo(BridgeEventTypes.BRIDGE_READY);
    }

    @Test
    @DisplayName("handleIncomingCommand PING responde status")
    void ping_returnsStatus() {
        when(publisher.activeClients()).thenReturn(1);
        when(scalePort.isAvailable()).thenReturn(true);
        when(printerPort.isAvailable()).thenReturn(true);
        ObjectNode ping = new ObjectMapper().createObjectNode();
        ping.put("type", BridgeEventTypes.PING);
        var response = service.handleIncomingCommand("s1", ping);
        assertThat(response.type()).isEqualTo(BridgeEventTypes.BRIDGE_READY);
    }

    @Test
    @DisplayName("SCANNER_INPUT reenvía a todos los clientes")
    void scannerInput_relaysToAll() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("type", BridgeEventTypes.SCANNER_INPUT);
        ObjectNode payload = root.putObject("payload");
        payload.put("code", "7701234567890");
        payload.put("source", "HID");

        var response = service.handleIncomingCommand("s1", root);

        assertThat(response.type()).isEqualTo(BridgeEventTypes.SCANNER_INPUT);
        org.mockito.Mockito.verify(publisher).publishToAll(org.mockito.ArgumentMatchers.any());
    }
}
