# ADR 0008: Use PostgreSQL as Primary Application Database

**Date:** 2026-05-24  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Malstrek's race timing events require both:
- **Immutable event capture** via Kafka (append-only)
- **Mutable data corrections** via manual operator UI (timestamp corrections, bib number edits, duplicate flag management)

Currently, the system uses:
- **MariaDB** for application data (races, starters, finishers)
- **PostgreSQL** for Metabase analytics infrastructure

The operational requirement to manually correct timestamps (when operators hit enter at wrong times), missed bib numbers, and manage duplicates (runners who unofficially re-enter the track after they finish) necessitates strong ACID transaction guarantees, row-level locking, and audit trail support.

### Current Constraints
- Manual corrections require immediate, consistent updates
- Metabase dashboard auto-refreshes every 5-10 seconds during races (heavy analytics polling)
- Potential for scaling to thousands of events/day as races grow
- Two database engines to manage and maintain

### Future Considerations
- **Immutable event architecture**: If deduplication/validation can be automated, events could become immutable
- **TimescaleDB migration**: Once events are immutable and scale exceeds 100K events/day, TimescaleDB becomes viable for superior time-series performance
- **Current trajectory**: Small races now, but designed for scale

## Options Considered

### 1. Keep MariaDB (Status Quo)
- **Pros**: No migration effort, team familiarity
- **Cons**: Difficult future migration to TimescaleDB; suboptimal for analytics (Metabase polling); requires tuning for dual workloads (OLTP + read-heavy)

### 2. Single PostgreSQL Instance (Both App + Metabase Schemas)
- **Pros**: One database engine, simpler operations initially
- **Cons**: Single point of failure; resource contention (OLTP writes vs. OLAP analytics); can't optimize independently; scaling flexibility lost at high volume

### 3. PostgreSQL + ClickHouse (Analytics Acceleration)
- **Pros**: Ultra-fast analytics queries, extreme scale capability
- **Cons**: Premature optimization; requires ETL pipeline; eventual consistency; 3 databases to manage; overkill for current scale (< 100K events/day)

### 4. PostgreSQL + TimescaleDB (Immediate Fit)
- **Pros**: Designed for time-series; automatic compression aligns with **current** operational pattern (corrections bounded to 72–96 hours post-race); hypertables support full UPDATE/DELETE during hot window, then compress cold chunks; superior analytics performance for Metabase's 5–10 sec refresh cycle; eliminates future migration path
- **Cons**: Minimal—TimescaleDB is a PostgreSQL extension with negligible overhead; learning curve is very small for PostgreSQL-familiar teams

### 5. **Separate PostgreSQL Instances with TimescaleDB (App) + PostgreSQL (Metabase)** ✅ (Selected)
- **Pros**: Clear separation of concerns; resilience; TimescaleDB on app instance is *immediate fit* (not deferred)—72-hour mutability window + heavy analytics queries (5–10 sec refresh) justify time-series optimization *now*; infrastructure flexibility for future OLAP separation; eliminates future PostgreSQL → TimescaleDB migration
- **Cons**: Two database instances to manage (minimal overhead gain from TimescaleDB adoption)

## Decision

**Adopt TimescaleDB (PostgreSQL extension) as the primary application database at initial deployment, with separate instances for app and Metabase:**
- **malstrek-db**: TimescaleDB instance (PostgreSQL + `timescaledb` extension) for race timing application data
- **metabase-db**: PostgreSQL (existing, no change required)

This replaces MariaDB with TimescaleDB to:
1. Support mutable data operations (UPDATE for timestamp/bib corrections) within a bounded operational window (0–72 hours post-race) with built-in hot/warm/cold tiering
2. Provide strong ACID guarantees and row-level locking for concurrent edits during active races
3. Optimize analytics workload: Metabase's 5–10 second refresh cycle benefits immediately from automatic time-based partition pruning and native INTERVAL type support
4. Ensure operational resilience (app and analytics failure are independent via separate `metabase-db` instance)
5. Eliminate future migration effort: adopt time-series optimization *now* instead of deferring to 10K+ events/day; eliminates PostgreSQL → TimescaleDB hop
6. Simplify the stack (one database engine + time-series extension instead of MariaDB + PostgreSQL)
7. Future-proof: clear path to separate analytics infrastructure (read replicas, ClickHouse) without application changes when event volume justifies it

**Deployment strategy:** Deploy TimescaleDB immediately (minimal risk, immediate operational benefits). Future scaling at 100K+ events/day may warrant separate analytics database, but application schema and queries remain unchanged.

## Rationale

### Workload-Driven Adoption (Not Volume-Driven)

Adoption criteria for TimescaleDB should be based on *operational workload patterns*, not solely event volume:

#### 1. **Operational Pattern Fit** ✅ (TODAY)
Race events exhibit **bounded mutability**—a current operational requirement:
- **Hot rows** (0–72 hours post-race): Full mutability; operators correct timestamps, bib numbers, manage duplicates via soft delete flag
- **Cold rows** (72+ hours): Effectively immutable per business SLA; no corrections allowed

The 72-hour window is **the operational requirement**. TimescaleDB hypertables are well-suited for this pattern:
- Full UPDATE/DELETE on recent (hot) chunks without performance penalty
- Automatic compression of cold chunks after 72 hours (e.g., `compress_after: '72 hours'`)
- Transparent queries across hot and compressed chunks
- **Zero application code changes**: Schema and queries remain identical

**Future flexibility**: If business requirements change and longer correction windows are needed, compressed chunks can still be decompressed and updated, though with a performance cost. This is an architectural option, not a constraint.

#### 2. **Analytics Workload Fit** ✅ (TODAY)
Metabase dashboard auto-refreshes every 5–10 seconds during active races. This heavy analytics polling benefits from time-series optimization:
- **Time-based partitioning**: Automatic daily chunks mean dashboard queries potentially scan fewer relevant chunks
- **Native INTERVAL support**: Timestamp subtraction yields database-native `INTERVAL` type, enabling efficient range queries
- **Partition pruning**: Time-range queries can exclude irrelevant chunks, reducing I/O

**Note**: These benefits are real but modest at current scale (< 1K events/day). The primary motivation for TimescaleDB adoption is operational pattern fit (bounded 72-hour mutability), not immediate analytics performance gains.

#### 3. **Immediate Setup (No Cost)**
```sql
CREATE EXTENSION timescaledb;  -- 1-time, 5 min, no downtime
SELECT create_hypertable('finishers', 'timestamp', if_not_exists => TRUE);
SELECT create_hypertable('starters', 'timestamp', if_not_exists => TRUE);
SELECT add_compression_policy('finishers', INTERVAL '72 hours');
SELECT add_compression_policy('starters', INTERVAL '72 hours');
```

**Operational overhead**: Negligible. TimescaleDB is a PostgreSQL extension; when not using time-series features, it adds ~5% overhead. Given the 72-hour window + analytics queries are *already in design*, this overhead is justified.

#### 4. **Eliminates Future Migration**
- **Without TimescaleDB now**: Deploy PostgreSQL → monitor → at 10K+ events/day, migrate to TimescaleDB (new extension, schema validation, query testing)
- **With TimescaleDB now**: Operational model is explicit and enforced from day 1; no future migration or schema changes needed

### Future Scaling (Volume-Driven)

Event volume thresholds inform *infrastructure separation*, not database adoption:

| Volume Range | Database Choice | Rationale |
|---|---|---|
| < 1K events/day | TimescaleDB (single instance) | Current state; compression inactive but operational pattern is explicit |
| 1K–10K events/day | TimescaleDB (single instance) | Hot chunks active; compression begins; monitor dashboard latency |
| 10K–50K events/day | TimescaleDB (single instance) + read replica | Partition count increases; consider read replica for analytics isolation |
| 50K–100K events/day | TimescaleDB (single instance) + separate analytics database (ClickHouse or read replica) | Hot data pressure increases; separate analytics workload from transactional writes |
| 100K+ events/day | TimescaleDB (app) + ClickHouse (analytics) + ETL pipeline | Pure append-only workload justifies columnar store; TimescaleDB remains for transactional correctness |

**Key point**: Schema and application code remain unchanged at all thresholds. Only *infrastructure topology* changes.

### Operational Resilience
- **Separation of Concerns**: Metabase runs against independent `metabase-db` instance; app infrastructure and analytics infrastructure have independent failure domains
- **Infrastructure Flexibility**: Separate instances enable future analytics optimization (e.g., read replica, dedicated OLAP database) without application changes
- **Stack Simplification**: One database engine (PostgreSQL) instead of MariaDB + PostgreSQL

### Implementation & Schema Design
- **Migration files**: Straightforward translation from MySQL → PostgreSQL SQL dialect
- **Application code**: Update JDBC connection URL and other config
- **Fresh deployment**: System is not yet in production; start with clean PostgreSQL schema (no data migration from MariaDB)

## Consequences

### Positive
- ✅ Strong ACID guarantees for mutable data (timestamps, bib numbers) with bounded mutability SLA
- ✅ Resilient architecture: Metabase infrastructure failure does not affect app
- ✅ Infrastructure flexibility: foundation for future OLAP database separation
- ✅ Immediate TimescaleDB adoption: Operational pattern and analytic workload justifies this fit
- ✅ Zero-friction upgrades: Migration to TimescaleDB requires no application code changes
- ✅ Simplified stack: PostgreSQL for both app and analytics (vs. MariaDB + Postgres today)
- ✅ Better integration with Metabase (same database technology)
- ✅ Community support: PostgreSQL ecosystem well-documented for time-series, transactional patterns, and compression strategies

### Negative
- ⚠️ Migration effort: SQL dialect translation (small effort given no production data)
- ⚠️ Operational learning: Team must be familiar with PostgreSQL (vs. MariaDB)
- ⚠️ Two database instances to monitor and backup (vs. single MariaDB today, but still simpler than MariaDB + Postgres)

## Deployment Strategy

### Phase 1: Initial Deployment (Now)
**Deploy TimescaleDB at PostgreSQL transition from MariaDB**

- **Setup** (5 minutes, no downtime):
  ```sql
  -- On PostgreSQL instance designated for app (malstrek-db)
  CREATE EXTENSION timescaledb;
  SELECT create_hypertable('finishers', 'timestamp', if_not_exists => TRUE);
  SELECT create_hypertable('starters', 'timestamp', if_not_exists => TRUE);
  SELECT add_compression_policy('finishers', INTERVAL '72 hours');
  SELECT add_compression_policy('starters', INTERVAL '72 hours');
  
  -- Metabase database (metabase-db) remains standard PostgreSQL
  ```

- **Configuration**:
  - OLTP tuning on `malstrek-db` (shared_buffers, effective_cache_size, checkpoint settings)
  - Enable background job for compression (default: every hour)
  - Set `timescaledb.compress_chunk_time_interval` to `'1 day'` for daily chunk sizing

- **Monitoring baselines**:
  - Dashboard refresh latency during active races (target: < 1 second)
  - Compression job duration (target: < 5 min)
  - Hot chunk size relative to `shared_buffers` (target: 2–4x for working set in memory)
  - Operator correction frequency (to validate 72-hour window assumption)

- **Operational documentation**:
  - Mutability SLA: "Operators have 72 hours post-race to correct timestamps, bib numbers, duplicates; after 72 hours, no further corrections are allowed per business policy"
  - Compression behavior: "Automatic compression after 72 hours; queries remain transparent across hot and cold chunks"
  - Backup strategy: Include compression status in backup metadata

### Phase 2: Monitor & Optimize (1K–10K events/day)

- **Metrics to track during active races**:
  - Dashboard query latency (track 50th, 95th, 99th percentile)
  - Transactional write latency (timestamp corrections, bib edits)
  - Lock wait times (contention between analytics and corrections)
  - CPU and I/O utilization

- **Go/No-Go Criteria for Phase 3**:
  - **Continue current setup** if:
    - Dashboard refresh consistently < 2 sec during peak race activity
    - No lock wait events > 100 ms
    - CPU < 70% at peak
  - **Proceed to Phase 3** if:
    - Dashboard refresh > 2–3 sec during peak race activity
    - Lock contention visible in logs (wait events > 100 ms)
    - CPU regularly > 80% during active races

### Phase 3: Analytics Workload Separation (10K–50K events/day, if needed)

- **Trigger**: Dashboard queries consistently slow (> 2–3 sec) despite compression; or lock contention during active races
- **Action** (zero application code changes):
  - Set up read replica of `malstrek-db` on dedicated hardware
  - Redirect Metabase to read replica (connection string change only)
  - Keep app writes to primary (malstrek-db)
  - Compression policy automatically replicates to replica

- **Result**: Analytics workload isolated; transactional writes unaffected

### Phase 4: High-Scale Analytics (100K+ events/day, if needed)

- **Trigger**: Read replica insufficient; cold data (30+ days) consuming significant storage/query budget
- **Action** (infrastructure change, no application changes):
  - Set up ClickHouse or separate OLAP-tuned PostgreSQL for analytics
  - Build ETL pipeline: `malstrek-db` (TimescaleDB, transactional) → OLAP database (analytics)
  - Update Metabase to query OLAP database
  - Eventually immutable pattern (events never UPDATE/DELETE post-72h) justifies columnar store

- **Timeline**: Separate ADR for analytics acceleration strategy

### Phase 5: Hypothetical Future—RFID-Based Automation (Beyond Current Scope)

If business architecture changes substantially (e.g., RFID-embedded bibs capture start/finish automatically with zero operator intervention), the system could move toward immutable event capture. Current manual correction requirement (72-hour SLA) makes this theoretical rather than near-term. TimescaleDB supports both mutable and immutable workloads, so this architectural path does not conflict with today's decision.

## Related Decisions & Future Work

- **ADR 0003**: Use JDBC Sink Connector for Event Persistence — **Still applicable** (PostgreSQL supported)
- **ADR 0004**: Use golang-migrate for Database Schema Versioning — **Still applicable** (database-agnostic)
- **Future ADR**: Audit Trail Mechanism — Document strategy for tracking timestamp/bib corrections (Splunk, triggers, row versioning, etc.)
- **Future ADR**: Analytics Acceleration Strategy — Evaluate ClickHouse at 100K+ events/day threshold

## Schema Refinement Opportunities (BIGINT → TIMESTAMP Migration)

The MySQL-to-PostgreSQL translation presents an opportunity to modernize the schema design.

### Current State (MySQL)
```sql
CREATE TABLE starters (
    starter_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    race_number INT UNSIGNED NOT NULL,
    timestamp BIGINT NOT NULL,  -- milliseconds since epoch
    deleted TINYINT(1) NOT NULL
);
```

### Proposed State (PostgreSQL)
```sql
CREATE TABLE starters (
    starter_id SERIAL PRIMARY KEY,
    race_number INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,  -- native PostgreSQL type
    deleted BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT idx_race_number FOREIGN KEY REFERENCES races(race_number)
);
CREATE INDEX idx_starters_race_number ON starters(race_number);
CREATE INDEX idx_starters_timestamp ON starters(timestamp);

CREATE TABLE finishers (
    finisher_id SERIAL PRIMARY KEY,
    race_number INT NOT NULL,
    bib_number INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT false,  -- soft delete; duplicates managed via this flag
);
CREATE INDEX idx_finishers_race_number ON finishers(race_number);
CREATE INDEX idx_finishers_timestamp ON finishers(timestamp);
```

### Refinements

1. **BIGINT → TIMESTAMP**
   - Eliminates millisecond arithmetic in every query (`/ 1000` conversions)
   - Enables native PostgreSQL timestamp functions
   - Dashboard query becomes simpler (no `SEC_TO_TIME()` conversion needed)
   - Confluent JDBC Sink Connector can handle `long` (ms) → `TIMESTAMP` conversion with appropriate `dialect.name` configuration; consult connector documentation for type mapping details

2. **TINYINT(1) → BOOLEAN**
   - Idiomatic PostgreSQL type, smaller storage footprint
   - Clearer intent (no ambiguity about numeric vs. boolean semantics)

3. **Explicit Indexes**
   - `race_number`: Required for dashboard joins and filtering
   - `timestamp`: Supports time-range queries and future time-series optimizations
   - Critical for query performance on growing datasets

4. **Integer Type Simplification**
   - MySQL: `INT UNSIGNED` (4-byte, 0 to 4.3B)
   - PostgreSQL: `INT` (4-byte, -2.1B to 2.1B, sufficient for race IDs and bib numbers)

### Dependencies: What Does NOT Change

**Avro Schema** (`start-line.avsc`, `finish-line.avsc`): No changes
- Continues to emit `"type": "long"` (milliseconds since epoch)
- Confluent JDBC Sink Connector handles type mapping to PostgreSQL `TIMESTAMP` (requires connector configuration)

**Application Code**: No changes required
- `System.currentTimeMillis()` continues to emit milliseconds
- Event assembly remains unchanged
- JDBC Sink Connector type mapping handles the conversion (see connector ops guide)

**Operator UI & Manual Corrections**: No changes to business logic
- Timestamp corrections, bib number edits, duplicate management all continue to work
- Soft deletes (`deleted` flag) remain the mechanism for managing duplicates

### Dashboard Query Impact

**Current (MySQL):**
```sql
SELECT
    DENSE_RANK() OVER (PARTITION BY race_number ORDER BY duration ASC) AS finishers,
    SEC_TO_TIME(ROUND((f.timestamp - s.timestamp) / 1000)) AS duration
FROM finishers AS f
INNER JOIN starters AS s USING (race_number);
```

**Refined (PostgreSQL):**
```sql
SELECT
    DENSE_RANK() OVER (PARTITION BY race_number ORDER BY (f.timestamp - s.timestamp) ASC) AS finishers,
    finisher_id,
    race_number,
    bib_number,
    (f.timestamp - s.timestamp) AS duration  -- native INTERVAL type
FROM finishers AS f
INNER JOIN starters AS s USING (race_number)
WHERE f.deleted <> 1
```

**Notes:**
- Join on `race_number` only (Kafka Connect does not provide `starter_id` to correlate individual start/finish pairs)
- `DENSE_RANK()` ranks finishers by duration (ascending finish times)
- Subtraction of two `TIMESTAMP` columns yields `INTERVAL` (database-native duration type)
- Soft deletes: `deleted` flag (active records `WHERE deleted <> 1`); duplicates managed by setting this flag
- No type conversions or millisecond arithmetic
- Cleaner, more maintainable SQL than MySQL equivalent

## Implementation & Operations

Detailed deployment, migration, and operational procedures are documented separately:
- **Deployment checklist & setup**: See deployment runbook (step-by-step setup, validation, monitoring)
- **Schema DDL & indexes**: Translation of the migration files in repository; other refinements to follow
- **JDBC Sink Connector configuration**: See Kafka/connector ops guide (PostgreSQL dialect configuration, type mapping)
- **Monitoring & runbooks**: PostgreSQL tuning, compression monitoring, operational metrics
- **Backup strategy**: Including compression metadata handling

This ADR establishes the architectural decision; implementation details are managed in operational documents that may evolve independently.
