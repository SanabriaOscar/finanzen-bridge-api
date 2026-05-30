package com.finnazen.bridge.exceptions;

import com.finnazen.bridge.shared.response.BridgeResponse;

public class BridgeException extends RuntimeException {

    private final BridgeResponse<Object> response;

    public BridgeException(BridgeResponse<Object> response) {
        super(response.description());
        this.response = response;
    }

    public BridgeResponse<Object> getResponse() {
        return response;
    }
}
