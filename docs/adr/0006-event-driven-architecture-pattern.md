# ADR 0006: Event-Driven Architecture Pattern

**Date:** 2025  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Malstrek must capture race events (runners starting and finishing) and make this data available to multiple systems: database storage, real-time analytics (Metabase), and potentially future integrations. The architecture must:

- Decouple event producers from consumers
- Support multiple consumers independently
- Enable asynchronous processing
- Handle events in sequence without data loss
- Scale as race event volume increases

### Options Considered

1. **Event-Driven Architecture** - Events published to message broker (Kafka), multiple independent consumers
2. **Request-Response Pattern** - Direct API calls to services, synchronous processing
3. **Batch Processing** - Collect events, process in batches on schedule
4. **CQRS (Command Query Responsibility Segregation)** - Separate write and read models
5. **Traditional Monolithic** - All processing in single application

## Decision

**Adopt Event-Driven Architecture as the primary design pattern.**

Architecture components:

```
Events Flow:
┌─────────────────────────────────────────────────────────────┐
│ Event Producers                                             │
├──────────────────────────────────────────────────────────────┤
│  - Race Start Event (Starter)                               │
│  - Race Finish Event (Finisher)                             │
│  - Source: UI (ConsoleUI) or external integrations         │
└────────────────────────┬────────────────────────────────────┘
                         │ Kafka Topics
       ┌─────────────────┼─────────────────┐
       ▼                 ▼                 ▼
   start-line        finish-line      (future topics)
   Topic             Topic
       │                 │
       ├─────────────────┴────────────────┐
       │                                  │
       ▼                                  ▼
  JDBC Sink              (Future Consumers)
  Connector              - Email Alerts
       │                 - Webhooks
       ▼                 - Real-time Dashboards
   MariaDB
       │
       ▼
   Metabase Analytics
```

## Rationale

- **Decoupling**: Event producers don't know about consumers; consumers are independent
- **Scalability**: New consumers can subscribe to events without modifying producers
- **Asynchronous Processing**: Events are processed independently, not blocking user actions
- **Reliability**: Kafka provides exactly-once semantics and event replay
- **Extensibility**: Easy to add new event consumers (analytics, notifications, etc.) without changing core application
- **Separation of Concerns**: Event capture (UI) is separate from persistence and analytics

## Consequences

### Positive
- Adding new event consumers (dashboards, alerts, integrations) requires zero changes to event producers
- Events are immutable and replay-able for debugging and recovery
- System is naturally resilient; failures in one consumer don't affect others
- Clear, traceable audit trail of all events
- Supports both real-time and batch processing on the same event stream
- Enables future features: delayed processing, event transformations, enrichment

### Negative
- Introduces complexity: distributed system with multiple failure points
- Requires understanding of event-driven concepts (topics, partitions, offsets, consumers)
- Debugging is harder: need to trace events through multiple systems
- Operational overhead: Kafka broker, Schema Registry, monitoring
- Potential for data inconsistency if consumers process events at different rates

## Event Types

### Starter Event (start-line topic)
- Published when runner crosses start line
- Schema: Avro (`start-line.avsc`)
- Fields: race_number, timestamp, deleted flag

### Finisher Event (finish-line topic)
- Published when runner crosses finish line
- Schema: Avro (`finish-line.avsc`)
- Fields: race_number, bib_number, timestamp, deleted flag

## Event Flow Guarantees

- **At-Least-Once Delivery**: Events are processed at least once (retries on failure)
- **Exactly-Once Semantics (JDBC Connector)**: JDBC connector ensures no duplicates in database
- **Order Preservation**: Events from same partition maintain order
- **Immutability**: Events cannot be modified once published

## Best Practices

1. **Event Schema Versioning**: Always include version in schema; support backward compatibility
2. **Event Timestamps**: Use server-time for consistency, not client time
3. **Idempotent Consumers**: Ensure consuming same event twice has same result
4. **Dead Letter Queues**: Route failed events to separate topic for manual inspection
5. **Monitoring**: Track event lag, consumer lag, and processing latency

## Future Extensions

The event-driven foundation enables:
- **Event Sourcing**: Store all events as source of truth, rebuild state on demand
- **CQRS**: Separate read models for different query patterns
- **Temporal Queries**: "What was the state at time X?"
- **Event Enrichment**: Add contextual data (weather, location) to events mid-stream
- **Complex Event Processing**: Multi-event correlations (e.g., detect abandoned races)

## Related Decisions

- ADR 0001: Use Kafka for Event Streaming
- ADR 0002: Use Apache Avro for Event Serialization
- ADR 0003: Use JDBC Sink Connector for Event Persistence
