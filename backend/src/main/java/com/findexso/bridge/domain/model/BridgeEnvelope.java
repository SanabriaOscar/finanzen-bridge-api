package com.findexso.bridge.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

public record BridgeEnvelope(
        int schemaVersion,
        String type,
        JsonNode payload,
        long timestamp
) {
}
