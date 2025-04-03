package com.example.log_aggregator.model;

import java.time.Instant;

public class LogResponse {
    private Instant timestamp;
    private String message;

    public LogResponse() {
    }

    public LogResponse(Instant timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public static LogResponse fromLogEntry(LogEntry logEntry) {
        return new LogResponse(logEntry.getTimestamp(), logEntry.getMessage());
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}