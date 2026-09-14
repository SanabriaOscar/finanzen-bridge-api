package com.findexso.bridge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Component
public class BridgeBrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(BridgeBrowserLauncher.class);

    private final BridgeProperties properties;

    public BridgeBrowserLauncher(BridgeProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openAdminPanel() {
        if (!properties.openBrowserOnStart()) {
            return;
        }
        String url = "http://" + properties.host() + ":" + properties.port() + "/";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                log.info("Panel bridge abierto en {}", url);
            }
        } catch (Exception ex) {
            log.info("Abra manualmente el panel admin: {}", url);
        }
    }
}
