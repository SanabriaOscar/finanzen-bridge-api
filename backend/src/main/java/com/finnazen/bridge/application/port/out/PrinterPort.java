package com.finnazen.bridge.application.port.out;

import com.finnazen.bridge.domain.model.PrintTicketCommand;

public interface PrinterPort {

    boolean isAvailable();

    void printTicket(PrintTicketCommand command);

    void printTestPage();

    void openCashDrawer();
}
