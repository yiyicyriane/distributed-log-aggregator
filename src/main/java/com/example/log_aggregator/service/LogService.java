package com.example.log_aggregator.service;

import com.example.log_aggregator.model.LogEntry;
import com.example.log_aggregator.model.LogResponse;
import com.example.log_aggregator.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * validate and save a log entry
     *
     * @param logEntry the log entry to save
     */
    public void saveLog(LogEntry logEntry) {
        if (logEntry == null) {
            throw new IllegalArgumentException("Log entry cannot be null");
        }

        if (logEntry.getServiceName() == null || logEntry.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }

        if (logEntry.getTimestamp() == null) {
            // if timestamp is missing, set current time
            logEntry.setTimestamp(Instant.now());
        }

        if (logEntry.getMessage() == null) {
            throw new IllegalArgumentException("Log message cannot be null");
        }

        logger.debug("Saving log entry: {}", logEntry);
        logRepository.save(logEntry);
    }

    /**
     * retrieves log for a given service within a specified time range
     *
     * @param serviceName the name of the service
     * @param startTime   start time (inclusive)
     * @param endTime     end time (exclusive)
     * @return list of log response objects matching the query
     */
    public List<LogResponse> queryLogs(String serviceName, Instant startTime, Instant endTime) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }

        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }

        logger.debug("Querying logs for service: {}, from: {} to: {}", serviceName, startTime, endTime);

        List<LogEntry> logs = logRepository.findByServiceNameAndTimeRange(serviceName, startTime, endTime);

        // map LogEntry objects to LogResponse DTOs
        return logs.stream()
                .map(LogResponse::fromLogEntry)
                .collect(Collectors.toList());
    }

    /**
     * set scheduled task to remove expired log entries, run every 5 mins
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredLogs() {
        logger.debug("Running scheduled cleanup of expired logs");
        logRepository.removeExpiredLogs();
    }
}
