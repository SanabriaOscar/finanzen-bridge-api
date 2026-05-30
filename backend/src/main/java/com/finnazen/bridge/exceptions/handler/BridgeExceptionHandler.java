package com.finnazen.bridge.exceptions.handler;

import com.finnazen.bridge.exceptions.BridgeException;
import com.finnazen.bridge.shared.response.BridgeResponse;
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getResponse());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BridgeResponse<Object>> handleGeneric(Exception ex) {
        log.error("Error bridge: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BridgeResponse<>(500, ex.getMessage(), null));
    }
}
