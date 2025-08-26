package com.bng.ussd.util;

import java.time.Instant;

public class CacheEntry {
    private final String sessionId;
    private final Instant timestamp;

    public CacheEntry(String sessionId, Instant timestamp) {
        this.sessionId = sessionId;
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}