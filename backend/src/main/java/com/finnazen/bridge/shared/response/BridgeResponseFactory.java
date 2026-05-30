package com.finnazen.bridge.shared.response;

public final class BridgeResponseFactory {

    private BridgeResponseFactory() {
    }

    public static <T> BridgeResponse<T> ok(String description, T data) {
        return new BridgeResponse<>(200, description, data);
    }

    public static <T> BridgeResponse<T> badRequest(String description) {
        return new BridgeResponse<>(400, description, null);
    }

    public static <T> BridgeResponse<T> error(String description) {
        return new BridgeResponse<>(500, description, null);
    }
}
