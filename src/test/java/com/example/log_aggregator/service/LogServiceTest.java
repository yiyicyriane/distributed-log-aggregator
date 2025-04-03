package com.example.log_aggregator.service;

import com.example.log_aggregator.model.LogEntry;
import com.example.log_aggregator.model.LogResponse;
import com.example.log_aggregator.repository.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    private LogService logService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logService = new LogService(logRepository);
    }

    @Test
    void saveLogSuccess() {
        //verify that a valid log entry is passed to the repository
        LogEntry logEntry = new LogEntry("test-service", Instant.now(), "Test message");

        logService.saveLog(logEntry);

        verify(logRepository, times(1)).save(logEntry);
    }

    @Test
    void saveLogWithNullTimestamp() {
        //if timestamp is null, set it to current time
        LogEntry logEntry = new LogEntry("test-service", null, "Test message");

        logService.saveLog(logEntry);

        assertNotNull(logEntry.getTimestamp());
        verify(logRepository, times(1)).save(logEntry);
    }

    @Test
    void saveLogWithInvalidData() {
        //test null log entry
        assertThrows(IllegalArgumentException.class, () -> {
            logService.saveLog(null);
        });

        // test empty service name
        assertThrows(IllegalArgumentException.class, () -> {
            logService.saveLog(new LogEntry("", Instant.now(), "Test message"));
        });

        // test null message
        assertThrows(IllegalArgumentException.class, () -> {
            logService.saveLog(new LogEntry("test-service", Instant.now(), null));
        });
    }

    @Test
    void queryLogsSuccess() {
        //test given a service and valid time range
        String serviceName = "test-service";
        Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime = Instant.now();

        LogEntry log1 = new LogEntry(serviceName, startTime.plus(15, ChronoUnit.MINUTES), "Log 1");
        LogEntry log2 = new LogEntry(serviceName, startTime.plus(30, ChronoUnit.MINUTES), "Log 2");

        when(logRepository.findByServiceNameAndTimeRange(serviceName, startTime, endTime))
                .thenReturn(Arrays.asList(log1, log2));

        List<LogResponse> results = logService.queryLogs(serviceName, startTime, endTime);

        assertEquals(2, results.size());
        assertEquals(log1.getTimestamp(), results.get(0).getTimestamp());
        assertEquals(log1.getMessage(), results.get(0).getMessage());
        assertEquals(log2.getTimestamp(), results.get(1).getTimestamp());
        assertEquals(log2.getMessage(), results.get(1).getMessage());
    }

    @Test
    void queryLogsWithInvalidData() {
        Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime = Instant.now();

        //empty service name
        assertThrows(IllegalArgumentException.class, () -> {
            logService.queryLogs("", startTime, endTime);
        });

        //null start time
        assertThrows(IllegalArgumentException.class, () -> {
            logService.queryLogs("test-service", null, endTime);
        });

        //null end time
        assertThrows(IllegalArgumentException.class, () -> {
            logService.queryLogs("test-service", startTime, null);
        });

        // start time after end time
        assertThrows(IllegalArgumentException.class, () -> {
            logService.queryLogs("test-service", endTime, startTime);
        });
    }

    @Test
    void queryLogsEmptyResult() {
        //if no logs found in the range, return an empty list
        String serviceName = "test-service";
        Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime = Instant.now();

        when(logRepository.findByServiceNameAndTimeRange(serviceName, startTime, endTime))
                .thenReturn(Collections.emptyList());

        List<LogResponse> results = logService.queryLogs(serviceName, startTime, endTime);

        assertTrue(results.isEmpty());
    }

    @Test
    void cleanupExpiredLogs() {
        logService.cleanupExpiredLogs();

        verify(logRepository, times(1)).removeExpiredLogs();
    }
}
