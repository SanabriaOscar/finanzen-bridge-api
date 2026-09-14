package com.findexso.bridge.application.port.out;

import com.findexso.bridge.domain.model.PrintTicketCommand;

public interface PrinterPort {

    boolean isAvailable();

    void printTicket(PrintTicketCommand command);

    void printTestPage();

    void openCashDrawer();
}
