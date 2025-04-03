package com.example.log_aggregator.controller;

import com.example.log_aggregator.model.LogEntry;
import com.example.log_aggregator.model.LogResponse;
import com.example.log_aggregator.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * endpoint to ingest a log entry
     *
     * @param logEntry The log entry containing service name, timestamp, and message.
     * @return http 201 created if the log is sorted successfully
     */
    @PostMapping
    public ResponseEntity<String> ingestLog(@RequestBody LogEntry logEntry) {
        try {
            logService.saveLog(logEntry);
            return ResponseEntity.status(HttpStatus.CREATED).body("Log ingested successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Error ingesting log: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error ingesting log", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing log", e);
        }
    }

    /**
     * @param service service name
     * @param start   start timestamp in ISO 8601 format
     * @param end     end timestamp in ISO 8601 format
     * @return a list of log entries that match the query
     */
    @GetMapping
    public ResponseEntity<List<LogResponse>> queryLogs(
            @RequestParam("service") String service,
            @RequestParam("start") String start,
            @RequestParam("end") String end) {

        try {
            Instant startTime = Instant.parse(start);
            Instant endTime = Instant.parse(end);

            List<LogResponse> logs = logService.queryLogs(service, startTime, endTime);
            return ResponseEntity.ok(logs);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid date format. Please use ISO 8601 format (e.g. 2025-03-17T10:15:00Z)", e);
        } catch (IllegalArgumentException e) {
            logger.error("Error querying logs: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error querying logs", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error querying logs", e);
        }
    }
}
