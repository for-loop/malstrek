# ADR 0004: Use golang-migrate for Database Schema Versioning

**Date:** 2025  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Malstrek must manage database schema changes (creating tables, indexes, modifying columns) in a version-controlled, reproducible way. The migration tool must:

- Support both up/down migrations
- Maintain migration history in the database
- Be language-agnostic (team uses multiple languages)
- Work reliably in containerized environments
- Prevent concurrent migrations (locking)

### Options Considered

1. **golang-migrate** - Lightweight, language-agnostic CLI tool with no runtime dependencies
2. **Flyway** - Java-based, requires JVM runtime
3. **Liquibase** - Java-based with XML/YAML configuration
4. **Custom SQL Scripts** - Manual orchestration, error-prone
5. **Hibernate/JPA Migrations** - ORM-integrated, ties migrations to Java layer

## Decision

**Adopt golang-migrate as the database schema versioning tool.**

Migrations are stored in the `migrations/` directory as numbered SQL files:
- Naming convention: `NNNN_description.{up,down}.sql`
- Current migrations cover: races, starters, finishers, race groups, timezones, race distances, race types

## Rationale

- **Language-Agnostic**: Written in Go, compiled to a single binary; works with any database
- **Zero Dependencies**: No JVM, runtime libraries, or frameworks required
- **Docker-Friendly**: Minimal Alpine Linux image for migrate container
- **Simplicity**: Plain SQL files are transparent and version-controllable
- **Reliability**: Migrations are idempotent and support rollback
- **Lock Support**: Built-in database locking prevents concurrent migrations
- **Composability**: Runs seamlessly in Docker Compose pipeline

## Consequences

### Positive
- Migrations are human-readable SQL, not abstracted configuration
- Git history directly reflects database changes
- Easy to review migration diffs before deployment
- Stateless tool; no configuration file needed
- Works with any SQL database (MySQL, PostgreSQL, etc.)

### Negative
- Requires SQL knowledge; less abstraction than ORM migrations
- No high-level migration primitives (must write raw SQL for complex operations)
- Migration tooling is not integrated with application code
- Team members need CLI tool familiarity

## Migration Structure

Each migration consists of two files:

**Up Migration** (`NNNN_description.up.sql`):
```sql
CREATE TABLE races (
    race_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    race_number INT UNSIGNED NOT NULL UNIQUE
);
```

**Down Migration** (`NNNN_description.down.sql`):
```sql
DROP TABLE races;
```

## Deployment

Migrations run automatically in Docker Compose as part of the startup pipeline:

1. Database service starts
2. Migrate service runs migrations
3. Application service starts

```bash
./migrations
├── 0001_race_types.up.sql
├── 0001_race_types.down.sql
├── 0002_race_distances.up.sql
├── 0002_race_distances.down.sql
└── ... more migrations
```

## Best Practices

- Always create paired up/down migrations
- Keep migrations small and focused
- Test rollbacks before merging to main
- Never modify already-deployed migrations (create new ones instead)
- Use foreign key constraints to ensure referential integrity
- Add indexes concurrently in production environments

## Related Decisions

- ADR 0005: Containerization with Docker Multi-Stage Builds
