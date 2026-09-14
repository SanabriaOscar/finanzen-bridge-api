package com.findexso.bridge.shared.logging;

import com.findexso.bridge.shared.constants.BridgeLogMarkersConstants;
import org.slf4j.Logger;

public final class BridgeSupportLog {

    private static final int STACK_LINES = 20;

    private BridgeSupportLog() {
    }

    public static void errorMarked(Logger log, String marker, String eventType, Throwable throwable, Object... ctx) {
        String technical = throwable != null ? throwable.getMessage() : "";
        log.error("{} type={} | ctx={} | technical={}", marker, eventType, formatCtx(ctx), technical, throwable);
        if (throwable != null) {
            log.error("{} stack={}", marker, stackSummary(throwable, STACK_LINES));
        }
    }

    public static void errorBridge(Logger log, String eventType, Throwable throwable, Object... ctx) {
        errorMarked(log, BridgeLogMarkersConstants.BRIDGE, eventType, throwable, ctx);
    }

    public static void errorHardware(Logger log, String eventType, Throwable throwable, Object... ctx) {
        errorMarked(log, BridgeLogMarkersConstants.HARDWARE, eventType, throwable, ctx);
    }

    public static void errorWebSocket(Logger log, String eventType, Throwable throwable, Object... ctx) {
        errorMarked(log, BridgeLogMarkersConstants.WEBSOCKET, eventType, throwable, ctx);
    }

    private static String formatCtx(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(keyValues[i]).append('=').append(keyValues[i + 1]);
        }
        return sb.toString();
    }

    static String stackSummary(Throwable t, int maxLines) {
        if (t == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
        StackTraceElement[] stack = t.getStackTrace();
        int limit = Math.min(maxLines, stack.length);
        for (int i = 0; i < limit; i++) {
            sb.append(System.lineSeparator()).append("  at ").append(stack[i]);
        }
        return sb.toString();
    }
}
