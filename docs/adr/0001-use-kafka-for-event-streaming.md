# ADR 0001: Use Kafka for Event Streaming

**Date:** 2025  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Malstrek needs to capture race event data (runners starting and finishing) from multiple sources and distribute this data reliably to persistent storage and analytics systems. The system must handle:

- Multiple concurrent race events
- Real-time event processing
- Reliable event delivery (no lost events)
- Horizontal scalability
- Decoupling of event producers from consumers

### Options Considered

1. **Kafka** - Distributed event streaming platform with partitioning, replication, and schema registry support
2. **RabbitMQ** - Message broker with reliable delivery but less event-centric
3. **Direct Database Writes** - Simple but creates tight coupling, poor scalability
4. **HTTP Event APIs** - Adds latency and complexity for high-volume scenarios

## Decision

**Adopt Apache Kafka as the primary event streaming platform.**

Specifically:
- Use Kafka for the start-line and finish-line event topics
- Integrate Confluent Schema Registry for schema management
- Use Kafka Connect with JDBC Sink Connector for persistence

## Rationale

- **Event-Driven Architecture**: Kafka is purpose-built for event streaming, making it ideal for race timing events
- **Reliability**: Kafka topics are replicated and persisted to disk, ensuring no events are lost
- **Decoupling**: Event producers (race timing UI) are completely decoupled from consumers (database, Metabase analytics)
- **Scalability**: Kafka partitions enable horizontal scaling as event volume increases
- **Schema Management**: Schema Registry provides versioning and compatibility checks for event schemas
- **Ecosystem Integration**: Extensive connector ecosystem (JDBC, Elasticsearch, etc.) reduces custom integration code

## Consequences

### Positive
- Enables asynchronous event processing
- Supports future consumers (analytics, notifications, third-party integrations) without modifying producers
- Provides built-in event replay capability for error recovery
- Clear separation of concerns between event capture and persistence

### Negative
- Adds operational complexity: requires running Kafka broker, Schema Registry, and managing topics
- Requires Docker Compose for local development
- Team must understand Kafka concepts (partitions, offsets, consumer groups)
- Additional infrastructure cost in production

## Related Decisions

- ADR 0002: Use Apache Avro for Event Serialization
- ADR 0003: Use JDBC Sink Connector for Event Persistence
