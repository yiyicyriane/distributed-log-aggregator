package com.example.log_aggregator.controller;

import com.example.log_aggregator.model.LogEntry;
import com.example.log_aggregator.model.LogResponse;
import com.example.log_aggregator.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LogControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public LogService logService() {
            return Mockito.mock(LogService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ingestLogSuccess() throws Exception {
        LogEntry logEntry = new LogEntry(
                "test-service",
                Instant.parse("2025-03-17T10:15:00Z"),
                "Test log message"
        );

        doNothing().when(logService).saveLog(any(LogEntry.class));

        // perform POST request and verify response
        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Log ingested successfully"));

        verify(logService, times(1)).saveLog(any(LogEntry.class));
    }

    @Test
    void ingestLogBadRequest() throws Exception {
        // create invalid log entry with missing service name
        LogEntry logEntry = new LogEntry(
                null,
                Instant.parse("2025-03-17T10:15:00Z"),
                "Test log message"
        );

        doThrow(new IllegalArgumentException("Service name cannot be empty"))
                .when(logService).saveLog(any(LogEntry.class));

        // expect 400 bad request
        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logEntry)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void queryLogsSuccess() throws Exception {
        String serviceName = "auth-service";
        Instant startTime = Instant.parse("2025-03-17T10:00:00Z");
        Instant endTime = Instant.parse("2025-03-17T10:30:00Z");

        LogResponse log1 = new LogResponse(
                Instant.parse("2025-03-17T10:05:00Z"),
                "User attempted login"
        );
        LogResponse log2 = new LogResponse(
                Instant.parse("2025-03-17T10:15:00Z"),
                "User login successful"
        );

        when(logService.queryLogs(serviceName, startTime, endTime))
                .thenReturn(Arrays.asList(log1, log2));

        // perform GET request and validate response JSON
        mockMvc.perform(get("/logs")
                        .param("service", serviceName)
                        .param("start", "2025-03-17T10:00:00Z")
                        .param("end", "2025-03-17T10:30:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].timestamp").value("2025-03-17T10:05:00Z"))
                .andExpect(jsonPath("$[0].message").value("User attempted login"))
                .andExpect(jsonPath("$[1].timestamp").value("2025-03-17T10:15:00Z"))
                .andExpect(jsonPath("$[1].message").value("User login successful"));

        verify(logService, times(1)).queryLogs(serviceName, startTime, endTime);
    }

    @Test
    void queryLogsEmptyResult() throws Exception {
        // simulate no logs found
        String serviceName = "unknown-service";
        Instant startTime = Instant.parse("2025-03-17T10:00:00Z");
        Instant endTime = Instant.parse("2025-03-17T10:30:00Z");

        when(logService.queryLogs(serviceName, startTime, endTime))
                .thenReturn(Collections.emptyList());

        // expect empty array response
        mockMvc.perform(get("/logs")
                        .param("service", serviceName)
                        .param("start", "2025-03-17T10:00:00Z")
                        .param("end", "2025-03-17T10:30:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void queryLogsInvalidDateFormat() throws Exception {
        // pass invalid timestamp format, expect 400 bad request
        mockMvc.perform(get("/logs")
                        .param("service", "test-service")
                        .param("start", "invalid-date")
                        .param("end", "2025-03-17T10:30:00Z"))
                .andExpect(status().isBadRequest());
    }
}