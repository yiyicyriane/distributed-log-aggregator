package com.example.log_aggregator.repository;

import com.example.log_aggregator.model.LogEntry;

import java.time.Instant;
import java.util.List;

public interface LogRepository {
    /**
     * persists a log entry to the underlying data store
     *
     * @param logEntry the log entry to be saved
     */
    void save(LogEntry logEntry);

    /**
     * Retrieves log entries for a specific service within the given time range.
     *
     * @param serviceName name of the service
     * @param startTime   start of the time range(inclusive)
     * @param endTime     end of the time range(exclusive)
     * @return a list of log entries matching the criteria, sorted by timestamp
     */
    List<LogEntry> findByServiceNameAndTimeRange(String serviceName, Instant startTime, Instant endTime);

    /**
     * Deletes log entries that are considered expired (older than one hour).
     */
    void removeExpiredLogs();
}
