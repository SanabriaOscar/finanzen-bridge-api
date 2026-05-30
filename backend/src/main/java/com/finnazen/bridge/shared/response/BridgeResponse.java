package com.finnazen.bridge.shared.response;

public record BridgeResponse<T>(int code, String description, T data) {
}
