package com.example.log_aggregator.repository;

import com.example.log_aggregator.model.LogEntry;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryLogRepository implements LogRepository {
    private final Map<String, List<LogEntry>> logsByService = new ConcurrentHashMap<>();

    private static final Duration LOG_EXPIRY_DURATION = Duration.ofHours(1);

    @Override
    public void save(LogEntry logEntry) {
        if (logEntry == null || logEntry.getServiceName() == null) {
            return; // ignore invalid entries
        }

        logsByService.computeIfAbsent(logEntry.getServiceName(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(logEntry);
    }

    @Override
    public List<LogEntry> findByServiceNameAndTimeRange(String serviceName, Instant startTime, Instant endTime) {
        if (serviceName == null || startTime == null || endTime == null) {
            return Collections.emptyList();
        }

        List<LogEntry> serviceLogs = logsByService.getOrDefault(serviceName, Collections.emptyList());

        // Ensure thread-safe access to the log list
        synchronized (serviceLogs) {
            return serviceLogs.stream()
                    .filter(log -> !log.getTimestamp().isBefore(startTime) && !log.getTimestamp().isAfter(endTime))
                    .sorted((log1, log2) -> log1.getTimestamp().compareTo(log2.getTimestamp()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void removeExpiredLogs() {
        Instant expiryThreshold = Instant.now().minus(LOG_EXPIRY_DURATION);

        // Iterate through all service logs and remove expired entries
        for (Map.Entry<String, List<LogEntry>> entry : logsByService.entrySet()) {
            List<LogEntry> logs = entry.getValue();

            // Ensure thread-safe modification
            synchronized (logs) {
                logs.removeIf(log -> log.getTimestamp().isBefore(expiryThreshold));
            }
        }
    }
}