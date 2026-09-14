package com.findexso.bridge.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PrintTicketCommand(
        @JsonProperty("ticketId") String ticketId,
        @JsonProperty("items") List<PrintTicketLine> items,
        @JsonProperty("total") double total,
        @JsonProperty("tenantId") Long tenantId,
        @JsonProperty("businessName") String businessName,
        @JsonProperty("customerName") String customerName,
        @JsonProperty("payMethodName") String payMethodName,
        @JsonProperty("sellerName") String sellerName,
        @JsonProperty("dateLabel") String dateLabel,
        @JsonProperty("paperWidthMm") Integer paperWidthMm,
        @JsonProperty("totalBase") Double totalBase,
        @JsonProperty("totalTax") Double totalTax,
        @JsonProperty("publicSaleId") String publicSaleId,
        @JsonProperty("printLines") List<FormattedPrintLine> printLines
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PrintTicketLine(
            @JsonProperty("name") String name,
            @JsonProperty("qty") double qty,
            @JsonProperty("price") double price
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FormattedPrintLine(
            @JsonProperty("text") String text,
            @JsonProperty("align") String align,
            @JsonProperty("bold") boolean bold
    ) {
    }
}
