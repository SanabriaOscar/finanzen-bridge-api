package com.finnazen.bridge.shared.logging;

import com.finnazen.bridge.shared.constants.BridgeLogMarkersConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class BridgeSupportLogTest {

    @Test
    void bridgeMarker_isGrepFriendly() {
        assertThat(BridgeLogMarkersConstants.BRIDGE).contains(BridgeLogMarkersConstants.GREP_BRIDGE);
    }

    @Test
    void errorBridge_doesNotThrow() {
        var log = LoggerFactory.getLogger(BridgeSupportLogTest.class);
        BridgeSupportLog.errorBridge(log, "TEST", new RuntimeException("printer offline"));
    }
}
