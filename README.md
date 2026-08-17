# malstrek

Log time of runners at the finish line of a small race. The name comes from målstrek, which means "finish line" in Norwegian

## Prerequisites

### Python Environment Setup

Install the modern environment manager:

```bash
brew install uv
```

Create a virtual environment and automatically install all code dependencies + dev tools:

```bash
uv venv --python 3.14
source .venv/bin/activate  # macOS/Linux
# or
.venv\Scripts\activate  # Windows

# Fast sync and editable install via uv instead of pip
uv pip install -e ".[dev]"
```

Run tests with coverage:

```bash
pytest
```

Format code:

```bash
black scripts/
```

Lint code:

```bash
pylint scripts/
mypy scripts/
```

* **Streamer Infrastructure**: https://github.com/for-loop/streamer
  - Must be running before starting malstrek
  - Provides Kafka broker, Schema Registry, and Kafka Connect

## First-time setup

### 1. Start the Kafka infrastructure (streamer)

From the streamer repo:

```bash
cd ../streamer
docker compose up -d
```

This starts the broker, Schema Registry, Kafka Connect, and supporting services.

### 2. Create and fill in the malstrek environment file

From the malstrek repo:

```bash
cp .env.example .env
```

Then edit `.env` and replace the placeholder values with your real settings, especially:
- `TIMESCALEDB_PASSWORD`
- `MB_DB_PASS`
- `POSTGRES_PASSWORD`
- any database credentials required by your local environment

### 3. Build images when Internet access is available

```bash
docker compose build
```

This matters for a race environment where you may later need to run without Internet access.

### 4. Start the non-interactive infrastructure services

```bash
docker compose up metabase metabase-db malstrek-db migrate-pg init-kafka --no-build --pull=never -d
```

This starts the database, migrations, Metabase, and the Kafka bootstrap job without starting the interactive console app.

The `init-kafka` service automatically does the following:
- ✅ Creates Kafka topics (`start-line`, `finish-line`)
- ✅ Registers Avro schemas with Schema Registry
- ✅ Creates JDBC Sink connectors

> `docker compose up -d` is fine for the first-time infrastructure bootstrap, but it is not the correct method for the interactive console app.

### 5. Start the interactive race app

```bash
docker compose run --rm malstrek-app
```

This is the correct startup method for the console app because it keeps stdin attached and waits for the user to provide input at the prompt, for example `Enter race number:`.

> Do not run the app with `docker compose up -d` because detached mode does not provide stdin, so the console scanner receives no input and exits with `No line found`.

## Usage

Once the services are running, the system is ready to:
1. Log race start events via the console UI
2. Stream runner finish times through Kafka
3. Persist data to TimescaleDB
4. View analytics in Metabase (http://localhost:3000)

## Configuration Reference

### Environment Variables

All configuration is managed through the `.env` file. See `.env.example` for all available options:

- **Kafka**: `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_CONNECT_URL`
- **Database**: `TIMESCALEDB_*` (TimescaleDB connection)
- **Metabase**: `MB_DB_*` (Metabase database configuration)
- **PostgreSQL**: `POSTGRES_*` (Container defaults)

## Development Commands

### View Service Status

```bash
docker compose ps
```

### View Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f malstrek-app
docker compose logs -f init-kafka
```

### Connect to Database

```bash
psql -h localhost -p 5432 -U <TIMESCALEDB_USER> -d <TIMESCALEDB_DB>
```

### List Kafka Topics

```bash
docker exec broker kafka-topics --bootstrap-server broker:29092 --list
```

### View Kafka Topic Content

```bash
docker exec broker kafka-console-consumer \
  --bootstrap-server broker:29092 \
  --topic start-line \
  --from-beginning \
  --property print.key=true
```

### Run Console App Locally

Build and run the Java application on your host (requires JDK 17+):

```bash
./gradlew run
```

Set these environment variables for local development:
```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_SCHEMA_REGISTRY_URL=http://localhost:8081
```

### Execute Commands in Container

```bash
# Start bash session in app container
docker exec -it malstrek-app bash

# Start bash session in database container
docker exec -it malstrek-db bash
```

## Manual Kafka Setup Reference

These commands are kept as a reference for manual debugging or quick development work when you want to create or inspect Kafka topics, schemas, or connectors without using the automated `init-kafka` bootstrap service.

### List topics

> This will wait until the broker is reachable.

```bash
docker exec -it broker kafka-topics --bootstrap-server broker:29092 --list
```

### Create topics

```bash
docker exec -it broker kafka-topics --create --topic start-line --bootstrap-server broker:29092 --partitions 1 --replication-factor 1
```

```bash
docker exec -it broker kafka-topics --create --topic finish-line --bootstrap-server broker:29092 --partitions 1 --replication-factor 1
```

### Register schema

```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data "{\"schema\": $(jq -Rs . ./app/src/main/avro/start-line.avsc)}" \
     http://localhost:8081/subjects/start-line-value/versions
```

```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data "{\"schema\": $(jq -Rs . ./app/src/main/avro/finish-line.avsc)}" \
     http://localhost:8081/subjects/finish-line-value/versions
```

## Kafka Connect Config

### Add connector

Required: manually edit `connection.user` and `connection.password` fields.

```bash
curl -X POST -H "Content-Type: application/json" --data @connector_malstrek-starter-sink_config.json http://localhost:8083/connectors
```

```bash
curl -X POST -H "Content-Type: application/json" --data @connector_malstrek-finisher-sink_config.json http://localhost:8083/connectors
```

See the Configuration Reference for the [JDBC Sink Connector](https://docs.confluent.io/kafka-connectors/jdbc/current/sink-connector/sink_config_options.html).

## Troubleshooting

### Re-Initialize Kafka Infrastructure

If you need to recreate topics/schemas/connectors:

```bash
# Stop everything
docker compose down

# Start streamer fresh
cd ../streamer
docker compose down
docker compose up -d

# Return and start malstrek
cd ../malstrek
docker compose up -d  # Automatic re-initialization
```

### View Connector Status

```bash
curl http://localhost:8083/connectors
curl http://localhost:8083/connectors/malstrek-starter-sink/status
curl http://localhost:8083/connectors/malstrek-finisher-sink/status
```

### Reset Database

To start with a fresh database (deletes all data):

```bash
docker compose down -v  # Remove volumes
docker compose up -d    # Recreate with migrations
```

## Architecture

### Service Topology

```
streamer (infrastructure)
    ├── Kafka Broker
    ├── Schema Registry
    ├── Kafka Connect
    └── PostgreSQL

malstrek (application)
    ├── init-kafka          (automatic setup)
    ├── malstrek-db         (TimescaleDB)
    ├── malstrek-app        (race timing app)
    ├── metabase            (analytics dashboard)
    └── metabase-db         (Metabase database)
```

### Data Flow

```
Console UI → Kafka Topics → JDBC Sink Connector → TimescaleDB → Metabase
```

**Topics**:
- `start-line`: Race start events
- `finish-line`: Runner finish times

**Tables**:
- `starters`: Start line recordings
- `finishers`: Finish line recordings
- Plus supporting tables: races, race_types, race_distances, race_groups, timezones

## Build

Build container images (requires Internet connection):

```bash
docker compose build
```

## Stop

Stop all services and remove containers:

```bash
docker compose down
```

Stop and remove data volumes (warning: deletes all data):

```bash
docker compose down -v
```

---

## Local Development (VS Code)

### Run on Host Machine

Requires:
- JDK 17+
- Gradle (via wrapper)
- Kafka running in containers

```bash
./gradlew run
```

### IDE Setup

The VS Code workspace is pre-configured. Open this folder in VS Code for best experience.

---

## Kafka Connect Connector Reference

### Automatic Connector Setup

Connectors are created automatically by the `init-kafka` service using templates:
- `connector_configs/starter-sink.template.json`
- `connector_configs/finisher-sink.template.json`

Environment variables are automatically substituted during setup.

### Manual Connector Management

For advanced use cases, manually manage connectors via the Kafka Connect REST API:

```bash
# View all connectors
curl http://localhost:8083/connectors

# View specific connector status
curl http://localhost:8083/connectors/malstrek-starter-sink/status

# Delete a connector
curl -X DELETE http://localhost:8083/connectors/malstrek-starter-sink

# Pause/Resume
curl -X PUT http://localhost:8083/connectors/malstrek-starter-sink/pause
curl -X PUT http://localhost:8083/connectors/malstrek-starter-sink/resume
```

See [JDBC Sink Connector Documentation](https://docs.confluent.io/kafka-connectors/jdbc/current/sink-connector/sink_config_options.html) for configuration options.

---


## Dashboard

Access the dashboard

http://localhost:3000

### First time

Follow the [official instructions](https://www.metabase.com/docs/latest/configuring-metabase/setting-up-metabase) and [malstrek dashboard instructions](docs/dashboard.md)

### Postgres (backend for Metabase)

Log on to the backend database for troubleshooting

```bash
docker exec -it postgres /bin/bash
```

Check version

```bash
psql --version
```

```bash
psql -h postgres -p 5432 -U <METABASE_DATABASE_USER> -d <METABASE_DATABASE_NAME>
```

Enter `METABASE_DATABASE_PASSWORD` when prompted

(Optional) Check what's in the database

```sql
\l          -- see a list of databases
\c postgres -- use database named postgres
\dt         -- see a list of tables
\q          -- quit
```

See data of interest

```sql
SELECT VERSION();
```