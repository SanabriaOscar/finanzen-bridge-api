package com.findexso.bridge.exceptions.handler;

import com.findexso.bridge.exceptions.BridgeException;
import com.findexso.bridge.shared.constants.BridgeConstants;
import com.findexso.bridge.shared.logging.BridgeSupportLog;
import com.findexso.bridge.shared.response.BridgeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BridgeExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BridgeExceptionHandler.class);

    @ExceptionHandler(BridgeException.class)
    public ResponseEntity<BridgeResponse<Object>> handleBridgeException(BridgeException ex) {
        BridgeSupportLog.errorBridge(log, "BRIDGE_BUSINESS", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getResponse());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BridgeResponse<Object>> handleGeneric(Exception ex) {
        BridgeSupportLog.errorBridge(log, "UNHANDLED", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BridgeResponse<>(500, BridgeConstants.MSG_PRINT_FAIL, null));
    }
}
