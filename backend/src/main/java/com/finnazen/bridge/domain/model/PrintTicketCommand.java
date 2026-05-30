package com.finnazen.bridge.domain.model;

import java.util.List;

public record PrintTicketCommand(
        String ticketId,
        List<PrintTicketLine> items,
        double total,
        Long tenantId
) {
    public record PrintTicketLine(String name, double qty, double price) {
    }
}
