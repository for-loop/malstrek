# Architecture Decision Records (ADRs) - Malstrek v1.0

This directory contains the Architecture Decision Records for the Malstrek project. ADRs document the significant architectural decisions made during the design and implementation of v1.0, including the rationale, consequences, and alternatives considered.

## Overview

Malstrek is an event-driven race timing application that captures runner start/finish events and makes them available for real-time tracking and analytics. The architecture combines modern event streaming (Kafka), schema management (Avro), and containerization (Docker) to create a scalable, maintainable system.

## Decision Records

### Core Architecture

| ID | Title | Status | Summary |
|---|---|---|---|
| [0001](0001-use-kafka-for-event-streaming.md) | Use Kafka for Event Streaming | Accepted | Apache Kafka provides reliable, scalable event distribution with built-in partitioning and schema registry integration. |
| [0006](0006-event-driven-architecture-pattern.md) | Event-Driven Architecture Pattern | Accepted | Adopts event-driven design to decouple producers from consumers and enable independent scaling. |

### Data & Serialization

| ID | Title | Status | Summary |
|---|---|---|---|
| [0002](0002-use-apache-avro-for-event-serialization.md) | Use Apache Avro for Event Serialization | Accepted | Avro provides compact binary serialization with built-in schema versioning and registry support. |
| [0004](0004-use-golang-migrate-for-database-schema-versioning.md) | Use golang-migrate for Database Schema Versioning | Accepted | golang-migrate provides language-agnostic, containerized database migration management. |

### Integration & Persistence

| ID | Title | Status | Summary |
|---|---|---|---|
| [0003](0003-use-jdbc-sink-connector-for-event-persistence.md) | Use JDBC Sink Connector for Event Persistence | Accepted | Kafka JDBC Sink Connector automatically writes events to database with zero custom code. |

### Technology Stack

| ID | Title | Status | Summary |
|---|---|---|---|
| [0005](0005-containerization-with-docker-multi-stage-builds.md) | Containerization with Docker Multi-Stage Builds | Accepted | Multi-stage Docker builds provide optimized images for development and production. |
| [0007](0007-java-17-and-gradle-build-system.md) | Java 17 and Gradle Build System | Accepted | Java 17 LTS with Gradle provides mature ecosystem and fast, flexible builds. |

## Key Architecture Decisions

### Event Flow

```
UI/Producers
    ↓
Kafka Topics (start-line, finish-line)
    ↓
JDBC Sink Connector
    ↓
MariaDB Database
    ↓
Metabase Analytics Dashboard
```

### Technology Stack

- **Language**: Java 17 (LTS)
- **Build Tool**: Gradle 8.14.2
- **Event Streaming**: Apache Kafka 3.3.1
- **Event Serialization**: Apache Avro 1.11.3
- **Schema Registry**: Confluent Schema Registry 7.9.0
- **Event Persistence**: Kafka JDBC Sink Connector
- **Database**: MariaDB 11.7.2
- **Schema Migration**: golang-migrate 4.18.3
- **Containerization**: Docker with multi-stage builds
- **Analytics**: Metabase 0.56.8
- **Testing**: JUnit 5.12.1, Mockito 5.2.0

## How to Use These Records

1. **For New Contributors**: Read in order [0006, 0001, 0002, 0003, 0004] to understand the overall architecture
2. **For Operations**: Focus on [0005, 0004] for deployment and infrastructure
3. **For Developers**: [0007, 0002, 0003] cover the build system and event structure
4. **For Refactoring**: Check related decisions in each ADR to avoid contradicting earlier choices

## When to Create New ADRs

Create a new ADR when:
- Making a significant architectural decision
- Choosing between multiple approaches
- Adding a new major component or technology
- Deprecating an existing decision

Do NOT create an ADR for:
- Implementation details or coding style
- Minor configuration changes
- Bug fixes

## Decision Status

- **Accepted**: Actively used in the current system
- **Superseded**: Replaced by a newer decision (previous ADR referenced)
- **Deprecated**: No longer used, but kept for historical context
- **Proposed**: Under consideration, not yet implemented

## Related Documentation

- [README.md](../README.md) - Project overview and setup instructions
- [dashboard.md](../dashboard.md) - Metabase analytics setup
- [Dockerfile](../../Dockerfile) - Container build stages
- [compose.yml](../../compose.yml) - Local development environment

## Contact

For questions about these decisions, please consult with the studio a priori or open an issue in the project repository.
