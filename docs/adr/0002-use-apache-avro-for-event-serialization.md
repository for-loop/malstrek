# ADR 0002: Use Apache Avro for Event Serialization

**Date:** 2025  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Malstrek events (Starter and Finisher) must be serialized for transmission through Kafka and storage in the database. The serialization format must:

- Support schema evolution (fields can be added/removed over time)
- Be space-efficient (events may be high volume)
- Support type safety and validation
- Integrate with Kafka Schema Registry for governance

### Options Considered

1. **Apache Avro** - Binary serialization format with schema registry support
2. **Protocol Buffers** - Binary format from Google, requires separate schema registry integration
3. **JSON** - Human-readable but verbose, schema validation not built-in
4. **Messagepack** - Compact but less schema-aware

## Decision

**Adopt Apache Avro as the event serialization format.**

Events are defined as Avro schemas:
- `start-line.avsc` - Starter event schema
- `finish-line.avsc` - Finisher event schema

Schemas are registered with Confluent Schema Registry at deployment time.

## Rationale

- **Schema Registry Integration**: Native support for schema versioning and compatibility checking
- **Compact Binary Format**: Avro's binary encoding is more efficient than JSON for high-volume events
- **Language Support**: Avro has first-class support in Java (our primary language)
- **Backward Compatibility**: Schema evolution is built-in with field defaults and union types
- **Kafka Ecosystem**: Confluent provides `kafka-avro-serializer` for seamless Kafka integration
- **JDBC Connector Support**: Kafka JDBC Sink Connector has native Avro support for schema-driven database inserts

## Consequences

### Positive
- Schema changes are tracked and versioned in the registry
- Events are smaller in size, reducing bandwidth and storage
- Schema compatibility is enforced at serialization/deserialization time
- Code generation from schemas reduces manual serialization code

### Negative
- Avro schemas are not human-readable (must use tools to inspect events)
- Requires Schema Registry to be operational
- Adding new event types requires schema registration before deployment
- Team must understand Avro schema language (AVSC)

## Schema Evolution Strategy

- Add new fields with `"default"` values for backward compatibility
- Never remove fields; mark as unused if needed
- Use union types (`["null", "type"]`) for optional fields
- Keep namespace consistent: `org.studioapriori.malstrek.avro`

## Related Decisions

- ADR 0001: Use Kafka for Event Streaming
- ADR 0003: Use JDBC Sink Connector for Event Persistence
