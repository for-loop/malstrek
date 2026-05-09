# ADR 0003: Use Kafka JDBC Sink Connector for Event Persistence

**Date:** 2025  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Events published to Kafka topics must be persisted to the relational database (MariaDB) for querying and analytics. The persistence mechanism must:

- Reliably write events from Kafka to database
- Handle backpressure and retries
- Support Avro schema-driven inserts
- Minimize operational overhead
- Enable integration with Metabase analytics

### Options Considered

1. **Kafka JDBC Sink Connector** - Confluent-managed connector with automatic schema handling
2. **Custom Consumer Application** - Write a Java consumer service to read and insert events
3. **Kafka Streams** - Stream processing for transformation before persistence
4. **Event Handlers in Application** - Application code directly persists events

## Decision

**Adopt Kafka JDBC Sink Connector (Confluent) as the primary persistence mechanism.**

Connector configuration includes:
- `connector_malstrek-starter-sink_config.json` - Routes `start-line` topic to `starters` table
- `connector_malstrek-finisher-sink_config.json` - Routes `finish-line` topic to `finishers` table

## Rationale

- **Schema-Driven**: Automatically maps Avro schema fields to database columns, eliminating manual mapping code
- **Operational Simplicity**: Configuration-based (JSON files) rather than custom application code
- **Reliability**: Handles batching, retries, error tolerance, and exactly-once semantics
- **Decoupling**: Event persistence is independent from event production
- **Maintainability**: Issues are documented in Confluent ecosystem, not buried in custom code
- **Analytics Ready**: Events land directly in database tables queryable by Metabase

## Consequences

### Positive
- Reduces application code complexity (no custom consumer logic needed)
- Events are immediately available in database for queries
- Connector can be configured and restarted independently of application
- Built-in error handling and monitoring

### Negative
- Requires Kafka Connect infrastructure (additional Docker container)
- Manual JSON configuration for each topic/table pair
- Debugging requires knowledge of Kafka Connect concepts and logs
- Connector updates tied to Confluent release cycle

## Connector Configuration Notes

The JDBC connector is configured with:
- **Connection Details**: MariaDB host, port, credentials
- **Table Names**: Maps topics to destination tables (`starters`, `finishers`)
- **Insert Mode**: Upsert or insert-only based on primary key strategy
- **Batch Size**: Balances latency vs. throughput
- **Error Handling**: Retry strategy for transient failures

Configuration files must be manually updated with database credentials before deployment:
```json
"connection.user": "YOUR_USER",
"connection.password": "YOUR_PASSWORD"
```

## Deployment

Connectors are registered with Kafka Connect via HTTP API:
```bash
curl -X POST -H "Content-Type: application/json" \
  --data @connector_malstrek-starter-sink_config.json \
  http://localhost:8083/connectors
```

## Related Decisions

- ADR 0001: Use Kafka for Event Streaming
- ADR 0002: Use Apache Avro for Event Serialization
- ADR 0004: Use golang-migrate for Database Schema Versioning
