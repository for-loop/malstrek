# Architecture Decision Records

Architecture Decision Records capture significant decisions for the Malstrek project.
Each numbered ADR is the source of truth for its title, status, and summary.

## Decision Records

The catalog and status definitions below are generated from the ADR files and catalog tool.
After adding an ADR or changing its status or summary, run
`python3 scripts/adr_catalog/check_adr_catalog.py` to regenerate this file. To add or
change an allowed status, update `STATUS_DEFINITIONS` in
`scripts/adr_catalog/check_adr_catalog.py` and regenerate.

<!-- ADR-CATALOG:START -->
| ID | Title | Status | Summary |
|---|---|---|---|
| [0001](0001-use-kafka-for-event-streaming.md) | Use Kafka for Event Streaming | Accepted | Use Kafka and Schema Registry to reliably distribute race events to downstream consumers. |
| [0002](0002-use-apache-avro-for-event-serialization.md) | Use Apache Avro for Event Serialization | Accepted | Use Apache Avro and Schema Registry to serialize race events with governed schemas. |
| [0003](0003-use-jdbc-sink-connector-for-event-persistence.md) | Use Kafka JDBC Sink Connector for Event Persistence | Accepted | Persist Kafka race events to the relational database with Kafka JDBC Sink Connector. |
| [0004](0004-use-golang-migrate-for-database-schema-versioning.md) | Use golang-migrate for Database Schema Versioning | Accepted | Manage database schema changes with versioned, reversible golang-migrate SQL migrations. |
| [0005](0005-containerization-with-docker-multi-stage-builds.md) | Containerization with Docker Multi-Stage Builds | Accepted | Use Docker multi-stage builds to package the application and its supporting tools. |
| [0006](0006-event-driven-architecture-pattern.md) | Event-Driven Architecture Pattern | Accepted | Use asynchronous events to decouple producers from database, analytics, and future consumers. |
| [0007](0007-java-17-and-gradle-build-system.md) | Java 17 and Gradle Build System | Accepted | Build the application with Java 17 and Gradle. |
| [0008](0008-use-timescaledb-as-primary-application-database.md) | Use TimescaleDB as Primary Application Database | Accepted | Adopt TimescaleDB as the primary application database for race data and corrections. |
| [0009](0009-keep-race-admin-in-the-same-repo-but-separate-by-app-and-service-boundary.md) | Keep race administration in the same repo, but separate by app and service boundary | Accepted | Keep race administration in this repository while separating the console, admin API, and UI into distinct apps. |
| [0010](0010-race-editor-command-driven-architecture.md) | Use a command-driven race-editor backend with Clean Architecture | Accepted | Build the race-editor backend around command-driven use cases and Clean Architecture boundaries. |
| [0011](0011-race-editor-domain-model-and-mvp-use-cases.md) | Define the race-editor domain model and MVP use cases | Accepted | Model finisher corrections as explicit domain operations with nullable bibs, soft deletion, and audit entries. |
<!-- ADR-CATALOG:END -->

## Decision Status

<!-- ADR-STATUSES:START -->
- **Proposed**: Under consideration
- **Accepted**: The decision is currently in force
- **Superseded**: Replaced or amended by a later ADR, which should be referenced
- **Deprecated**: No longer applicable
<!-- ADR-STATUSES:END -->

## Creating an ADR

Use the next available four-digit number in the filename and include a title, status,
and concise summary near the beginning of the document.
