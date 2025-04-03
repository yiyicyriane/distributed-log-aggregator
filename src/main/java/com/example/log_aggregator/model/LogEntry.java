package com.example.log_aggregator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

public class LogEntry {
    @JsonProperty("service_name")
    private String serviceName;
    private Instant timestamp;
    private String message;

    public LogEntry() {
    }

    public LogEntry(String serviceName, Instant timestamp, String message) {
        this.serviceName = serviceName;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogEntry logEntry = (LogEntry) o;
        return Objects.equals(serviceName, logEntry.serviceName) &&
                Objects.equals(timestamp, logEntry.timestamp) &&
                Objects.equals(message, logEntry.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, timestamp, message);
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "serviceName='" + serviceName + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}