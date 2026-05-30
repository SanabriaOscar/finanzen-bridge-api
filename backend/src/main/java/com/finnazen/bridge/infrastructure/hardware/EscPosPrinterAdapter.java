package com.finnazen.bridge.infrastructure.hardware;

import com.finnazen.bridge.application.port.out.PrinterPort;
import com.finnazen.bridge.config.BridgeProperties;
import com.finnazen.bridge.domain.model.PrintTicketCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    private volatile PrintService cachedService;

    public EscPosPrinterAdapter(BridgeProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isAvailable() {
        return resolvePrintService() != null;
    }

    @Override
    public void printTicket(PrintTicketCommand command) {
        int cols = lineWidth();
        EscPosEncoder enc = EscPosEncoder.create()
                .alignCenter()
                .bold(true)
                .line("FINNAZEN")
                .bold(false)
                .line("Ticket: " + truncate(safe(command.ticketId()), cols))
                .line(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .alignLeft()
                .separator(cols);

        if (command.items() != null) {
            for (PrintTicketCommand.PrintTicketLine item : command.items()) {
                String name = truncate(safe(item.name()), cols);
                enc.line(name);
                enc.line(String.format(Locale.US, "  %.2f x %.0f = %.0f",
                        item.qty(), item.price(), item.qty() * item.price()));
            }
        }

        enc.separator(cols)
                .bold(true)
                .line(String.format(Locale.US, "TOTAL: %.0f", command.total()))
                .bold(false)
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
                .line("FINNAZEN")
                .bold(false)
                .line("prueba finazen")
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

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + ".";
    }

    private int lineWidth() {
        return EscPosEncoder.columnsForPaperWidth(properties.printer().paperWidthMm());
    }
}
