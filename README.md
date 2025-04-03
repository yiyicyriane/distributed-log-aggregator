# Distributed Log Aggregator

A lightweight, memory-efficient service for collecting and querying logs from distributed microservices.

## Overview

This project implements a log ingestion and query service that collects logs from various microservices and provides a
simple API to query them by service name and time range. The system focuses on efficiency, thread safety, and proper
handling of concurrent requests.

## Features

- Fast log ingestion with a simple RESTful API
- Time-based log queries with service filtering
- Automatic cleanup of logs older than 1 hour
- Thread-safe implementation for concurrent access
- Proper timestamp ordering of logs (even when ingested out of order)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Application

Clone the repository and start the service:

```bash
git clone https://github.com/yiyicyriane/distributed-log-aggregator.git
cd distributed-log-aggregator
mvn spring-boot:run
```

The service runs on http://localhost:8080 by default.

## API Documentation

### Ingest Logs

```
POST /logs
```

**Request Body:**

```json
{
  "service_name": "auth-service",
  "timestamp": "2025-03-17T10:15:00Z",
  "message": "User login successful"
}
```

### Query Logs

```
GET /logs?service=<service_name>&start=<start_time>&end=<end_time>
```

**Parameters:**

- `service`: Name of the service
- `start`: Start timestamp (ISO 8601)
- `end`: End timestamp (ISO 8601)

**Response:**

```json
[
  {
    "timestamp": "2025-03-17T10:05:00Z",
    "message": "User attempted login"
  },
  {
    "timestamp": "2025-03-17T10:15:00Z",
    "message": "User login successful"
  }
]
```

## Quick Test

After starting the application, you can test the API with curl:

```bash
# Ingest a log
curl -X POST http://localhost:8080/logs \
  -H "Content-Type: application/json" \
  -d '{"service_name":"auth-service","timestamp":"2025-03-17T10:15:00Z","message":"User login successful"}'

# Query logs
curl "http://localhost:8080/logs?service=auth-service&start=2025-03-17T10:00:00Z&end=2025-03-17T11:00:00Z"
```

## Architecture

The application follows a standard layered architecture:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and validation
- **Repository Layer**: Manages log storage and retrieval

Data is stored in memory using thread-safe collections, with a scheduled task that removes expired logs every 5 minutes.

## Deployment Considerations

While this implementation uses in-memory storage, in a production environment you might want to:

- Add a persistent storage backend
- Implement log rotation and archiving
- Set up distributed deployment for high availability
- Add authentication/authorization
- Add metrics and monitoring

## Running Tests

```bash
mvn test
```
