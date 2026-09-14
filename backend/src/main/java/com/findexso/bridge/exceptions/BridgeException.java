package com.findexso.bridge.exceptions;

import com.findexso.bridge.shared.response.BridgeResponse;

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
