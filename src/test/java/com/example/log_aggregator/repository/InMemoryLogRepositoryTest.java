package com.example.log_aggregator.repository;

import com.example.log_aggregator.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryLogRepositoryTest {

    private InMemoryLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLogRepository();
    }

    @Test
    void saveAndFindLogs() {
        String serviceName = "test-service";
        Instant now = Instant.now();

        LogEntry log1 = new LogEntry(serviceName, now.minus(30, ChronoUnit.MINUTES), "Log message 1");
        LogEntry log2 = new LogEntry(serviceName, now.minus(20, ChronoUnit.MINUTES), "Log message 2");
        LogEntry log3 = new LogEntry(serviceName, now.minus(10, ChronoUnit.MINUTES), "Log message 3");

        repository.save(log1);
        repository.save(log2);
        repository.save(log3);

        //Query logs in full time range and verify order
        Instant startTime = now.minus(1, ChronoUnit.HOURS);
        Instant endTime = now;
        List<LogEntry> results = repository.findByServiceNameAndTimeRange(serviceName, startTime, endTime);

        assertEquals(3, results.size());
        assertEquals(log1, results.get(0)); // 应该按时间顺序排序
        assertEquals(log2, results.get(1));
        assertEquals(log3, results.get(2));

        // Query partial time range
        startTime = now.minus(25, ChronoUnit.MINUTES);
        endTime = now.minus(5, ChronoUnit.MINUTES);
        results = repository.findByServiceNameAndTimeRange(serviceName, startTime, endTime);

        assertEquals(2, results.size());
        assertEquals(log2, results.get(0));
        assertEquals(log3, results.get(1));
    }

    @Test
    void removeExpiredLogs() {
        String serviceName = "test-service";
        Instant now = Instant.now();

        LogEntry expiredLog1 = new LogEntry(serviceName, now.minus(2, ChronoUnit.HOURS), "Expired log 1");
        LogEntry expiredLog2 = new LogEntry(serviceName, now.minus(90, ChronoUnit.MINUTES), "Expired log 2");
        LogEntry validLog1 = new LogEntry(serviceName, now.minus(30, ChronoUnit.MINUTES), "Valid log 1");
        LogEntry validLog2 = new LogEntry(serviceName, now.minus(15, ChronoUnit.MINUTES), "Valid log 2");

        repository.save(expiredLog1);
        repository.save(expiredLog2);
        repository.save(validLog1);
        repository.save(validLog2);

        repository.removeExpiredLogs();

        Instant startTime = now.minus(3, ChronoUnit.HOURS);
        Instant endTime = now;
        List<LogEntry> results = repository.findByServiceNameAndTimeRange(serviceName, startTime, endTime);

        assertEquals(2, results.size());
        assertEquals(validLog1, results.get(0));
        assertEquals(validLog2, results.get(1));
    }

    @Test
    void handleMultipleServices() {
        String service1 = "service-1";
        String service2 = "service-2";
        Instant now = Instant.now();

        LogEntry log1 = new LogEntry(service1, now.minus(30, ChronoUnit.MINUTES), "Service 1 log 1");
        LogEntry log2 = new LogEntry(service1, now.minus(20, ChronoUnit.MINUTES), "Service 1 log 2");
        LogEntry log3 = new LogEntry(service2, now.minus(25, ChronoUnit.MINUTES), "Service 2 log 1");
        LogEntry log4 = new LogEntry(service2, now.minus(15, ChronoUnit.MINUTES), "Service 2 log 2");

        repository.save(log1);
        repository.save(log2);
        repository.save(log3);
        repository.save(log4);

        Instant startTime = now.minus(1, ChronoUnit.HOURS);
        Instant endTime = now;
        List<LogEntry> service1Results = repository.findByServiceNameAndTimeRange(service1, startTime, endTime);

        assertEquals(2, service1Results.size());
        assertEquals(log1, service1Results.get(0));
        assertEquals(log2, service1Results.get(1));
        
        List<LogEntry> service2Results = repository.findByServiceNameAndTimeRange(service2, startTime, endTime);

        assertEquals(2, service2Results.size());
        assertEquals(log3, service2Results.get(0));
        assertEquals(log4, service2Results.get(1));
    }
}
