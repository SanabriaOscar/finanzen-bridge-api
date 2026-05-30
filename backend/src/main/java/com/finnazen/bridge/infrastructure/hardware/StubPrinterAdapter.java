package com.finnazen.bridge.infrastructure.hardware;

import com.finnazen.bridge.application.port.out.PrinterPort;
import com.finnazen.bridge.config.BridgeProperties;
import com.finnazen.bridge.domain.model.PrintTicketCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "finnazen.bridge.printer", name = "enabled", havingValue = "false", matchIfMissing = true)
public class StubPrinterAdapter implements PrinterPort {

    private static final Logger log = LoggerFactory.getLogger(StubPrinterAdapter.class);

    private final BridgeProperties properties;

    public StubPrinterAdapter(BridgeProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isAvailable() {
        return properties.printer().enabled();
    }

    @Override
    public void printTicket(PrintTicketCommand command) {
        log.info("STUB PRINT ticketId={} total={} lines={}",
                command.ticketId(), command.total(),
                command.items() != null ? command.items().size() : 0);
    }

    @Override
    public void printTestPage() {
        log.info("STUB PRINT test page");
    }

    @Override
    public void openCashDrawer() {
        log.info("STUB OPEN cash drawer");
    }
}
