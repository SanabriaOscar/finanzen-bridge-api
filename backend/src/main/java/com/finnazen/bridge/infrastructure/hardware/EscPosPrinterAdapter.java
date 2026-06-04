package com.finnazen.bridge.infrastructure.hardware;

import com.finnazen.bridge.application.port.out.PrinterPort;
import com.finnazen.bridge.config.BridgeProperties;
import com.finnazen.bridge.config.BridgeRuntimePrinterConfig;
import com.finnazen.bridge.domain.model.PrintTicketCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.finnazen.bridge.shared.constants.ThermalPrintConstants;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "finnazen.bridge.printer", name = "enabled", havingValue = "true")
public class EscPosPrinterAdapter implements PrinterPort {

    private static final Logger log = LoggerFactory.getLogger(EscPosPrinterAdapter.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CO"));

    private final BridgeProperties properties;
    private final BridgeRuntimePrinterConfig runtimePrinterConfig;
    private volatile PrintService cachedService;

    public EscPosPrinterAdapter(BridgeProperties properties, BridgeRuntimePrinterConfig runtimePrinterConfig) {
        this.properties = properties;
        this.runtimePrinterConfig = runtimePrinterConfig;
    }

    @Override
    public boolean isAvailable() {
        return resolvePrintService() != null;
    }

    @Override
    public void printTicket(PrintTicketCommand command) {
        int cols = lineWidth(command);
        if (command.printLines() != null && !command.printLines().isEmpty()) {
            EscPosEncoder enc = EscPosEncoder.create();
            renderFormattedLines(enc, command.printLines(), cols);
            enc.blankLines(2).cutPartial();
            sendRaw(enc.toBytes());
            log.info("Ticket layout impreso ticketId={} lineas={} printer={}",
                    command.ticketId(), command.printLines().size(), printerName());
            return;
        }
        log.warn("PRINT_TICKET sin printLines; usando formato reducido ticketId={}", command.ticketId());
        printLegacyTicket(command, cols);
    }

    private void renderFormattedLines(EscPosEncoder enc,
                                      List<PrintTicketCommand.FormattedPrintLine> lines,
                                      int cols) {
        for (PrintTicketCommand.FormattedPrintLine row : lines) {
            if (row == null) {
                continue;
            }
            String text = row.text() != null ? row.text() : "";
            if (ThermalPrintConstants.ALIGN_CENTER.equalsIgnoreCase(row.align())) {
                enc.alignCenter();
            } else {
                enc.alignLeft();
            }
            enc.bold(row.bold());
            if (text.isBlank()) {
                enc.blankLines(1);
            } else {
                enc.line(text.length() <= cols ? text : truncate(text, cols));
            }
            enc.bold(false);
        }
    }

    private void printLegacyTicket(PrintTicketCommand command, int cols) {
        EscPosEncoder enc = EscPosEncoder.create()
                .alignCenter()
                .bold(true)
                .line(truncate(safe(command.businessName(), ThermalPrintConstants.DEFAULT_BUSINESS_NAME), cols))
                .bold(false);

        if (command.dateLabel() != null && !command.dateLabel().isBlank()) {
            enc.line(truncate(command.dateLabel(), cols));
        } else {
            enc.line(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        enc.line("Ticket: " + truncate(safe(command.ticketId()), cols))
                .alignLeft()
                .separator(cols);

        if (command.customerName() != null && !command.customerName().isBlank()) {
            enc.line("Cliente: " + truncate(command.customerName(), cols));
        }
        if (command.sellerName() != null && !command.sellerName().isBlank()) {
            enc.line("Vendedor: " + truncate(command.sellerName(), cols));
        }
        if (command.payMethodName() != null && !command.payMethodName().isBlank()) {
            enc.line("Pago: " + truncate(command.payMethodName(), cols));
        }
        enc.separator(cols);

        if (command.items() != null) {
            for (PrintTicketCommand.PrintTicketLine item : command.items()) {
                String name = truncate(safe(item.name()), cols);
                enc.line(name);
                double qty = item.qty();
                double subtotal = qty * item.price();
                if (qty > 1.001) {
                    enc.line(String.format(Locale.forLanguageTag("es-CO"),
                            "Cant: %.0f  V.unit: %,.0f  Total: %,.0f", qty, item.price(), subtotal));
                } else {
                    enc.line(String.format(Locale.forLanguageTag("es-CO"),
                            "V.unit: %,.0f  Total: %,.0f", item.price(), subtotal));
                }
            }
        }

        enc.separator(cols);
        if (command.totalBase() != null && command.totalTax() != null && command.totalTax() > 0) {
            enc.line(String.format(Locale.forLanguageTag("es-CO"), "Base: %,.0f  IVA: %,.0f",
                    command.totalBase(), command.totalTax()));
        }
        enc.bold(true)
                .line(String.format(Locale.forLanguageTag("es-CO"), "TOTAL A PAGAR: %,.0f", command.total()))
                .bold(false)
                .alignCenter()
                .line("Gracias por su compra")
                .line(ThermalPrintConstants.FOOTER_SOFTWARE)
                .blankLines(2)
                .cutPartial();

        sendRaw(enc.toBytes());
        log.info("Ticket impreso ticketId={} printer={}", command.ticketId(), printerName());
    }

    @Override
    public void printTestPage() {
        String now = DATE_FMT.format(java.time.LocalDateTime.now());
        byte[] data = EscPosEncoder.create()
                .alignCenter()
                .bold(true)
                .line(ThermalPrintConstants.TEST_PAGE_TITLE)
                .bold(false)
                .line(ThermalPrintConstants.TEST_PAGE_SUBTITLE)
                .line(now)
                .blankLines(2)
                .cutPartial()
                .toBytes();
        sendRaw(data);
        log.info("Pagina de prueba impresa en {}", printerName());
    }

    @Override
    public void openCashDrawer() {
        byte[] data = EscPosEncoder.create().openCashDrawer().toBytes();
        sendRaw(data);
    }

    public List<String> listPrinterNames() {
        return Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .map(PrintService::getName)
                .collect(Collectors.toList());
    }

    public String resolvedPrinterName() {
        PrintService service = resolvePrintService();
        return service != null ? service.getName() : null;
    }

    public String installationHint() {
        if (isAvailable()) {
            return "Impresora lista: " + resolvedPrinterName();
        }
        return """
                Windows no tiene instalada la XPrinter como impresora. \
                Si en Panel de control aparece solo "USB Printer Port" (No especificado), \
                instale el driver ESC/POS de Xprinter (58/80 mm), reinicie el bridge y verifique \
                que aparezca en Impresoras (no en No especificado). Luego configure \
                finnazen.bridge.printer.name con el nombre exacto.""";
    }

    private String buildPrinterNotFoundMessage() {
        return "Impresora no encontrada (buscando: " + properties.printer().name() + "). "
                + "Impresoras Windows: " + listPrinterNames() + ". "
                + installationHint();
    }

    private void sendRaw(byte[] data) {
        PrintService service = resolvePrintService();
        if (service == null) {
            throw new IllegalStateException(buildPrinterNotFoundMessage());
        }
        try {
            DocPrintJob job = service.createPrintJob();
            Doc doc = new SimpleDoc(data, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, null);
        } catch (PrintException ex) {
            throw new IllegalStateException("Error enviando datos a la impresora: " + ex.getMessage(), ex);
        }
    }

    private PrintService resolvePrintService() {
        if (cachedService != null) {
            return cachedService;
        }
        String configured = properties.printer().name();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (matches(service.getName(), configured)) {
                cachedService = service;
                return service;
            }
        }
        return null;
    }

    static boolean matches(String actualName, String configuredName) {
        if (actualName == null) {
            return false;
        }
        String actual = actualName.toLowerCase(Locale.ROOT);
        if (configuredName == null || configuredName.isBlank() || "default".equalsIgnoreCase(configuredName)) {
            return actual.contains("xprinter") || actual.contains("xp-") || actual.contains("pos");
        }
        return actual.contains(configuredName.toLowerCase(Locale.ROOT));
    }

    private String printerName() {
        PrintService service = resolvePrintService();
        return service != null ? service.getName() : properties.printer().name();
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static String safe(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + ".";
    }

    private int lineWidth(PrintTicketCommand command) {
        if (command.paperWidthMm() != null && command.paperWidthMm() > 0) {
            return EscPosEncoder.columnsForPaperWidth(command.paperWidthMm());
        }
        return EscPosEncoder.columnsForPaperWidth(runtimePrinterConfig.getPaperWidthMm());
    }

    private int lineWidth() {
        return EscPosEncoder.columnsForPaperWidth(runtimePrinterConfig.getPaperWidthMm());
    }
}
