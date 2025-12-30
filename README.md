# malstrek

Log time of runners at the finish line of a small race. The name comes from målstrek, which means "finish line" in Norwegian

## Prerequisites

* Kafka https://github.com/for-loop/streamer

## First time

Create `.env` file at the root directory

```bash
MARIADB_ROOT_PASSWORD=<YOUR_ROOT_USER_PASSWORD>
MYSQL_DATABASE=<DATABASE_NAME>
MYSQL_USER=<YOUR_USER_NAME>
MYSQL_PASSWORD=<YOUR_USER_PASSWORD>
MB_DB_TYPE=postgres
MB_DB_DBNAME=<METABASE_DATABASE_NAME>
MB_DB_PORT=5432
MB_DB_USER=<METABASE_DATABASE_USER>
MB_DB_PASS=<METABASE_DATABASE_PASSWORD>
MB_DB_HOST=metabase-db
POSTGRES_USER=<METABASE_DATABASE_USER>
POSTGRES_DB=<METABASE_DATABASE_NAME>
POSTGRES_PASSWORD=<METABASE_DATABASE_PASSWORD>
KAFKA_SCHEMA_REGISTRY_URL=http://schema-registry:8081
```

Add the following to the `.zshrc` or `.bashrc` (if running the non-containerized app)

```bash
export KAFKA_BOOTSTRAP_SERVERS=<KAFKA_BOOTSTRAP_SERVER>:9092
```

> For local development, set it to `0.0.0.0:9092`

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

See Configuration Reference for [JDBC Sink Connector](https://docs.confluent.io/kafka-connectors/jdbc/current/sink-connector/sink_config_options.html)

---

## Build

Build images (Internet connection required)

```bash
docker compose build
```

## Run

Run background containers (offline OK)

```bash
docker compose up db migrate metabase metabase-db --no-build --pull=never -d
```

Run the app

```bash
docker compose run --rm malstrek-app
```

Start bash prompt inside the container

```bash
docker exec -it db bash
```

Log onto the database

```bash
mariadb -u <YOUR_USER_NAME> -p
```

Switch database

```sql
USE <DATABASE_NAME>;
```

Exit the database

```sql
exit
```

Exit the bash prompt

```bash
exit
```

## Stop

```bash
docker compose down
```

---

## Run the console application within VS Code

In the Terminal,

```bash
./gradlew run
```

---

## Dashboard

Access the dashboard

```
http://localhost:3000
```

### First time

Follow the [instructions](https://www.metabase.com/docs/latest/configuring-metabase/setting-up-metabase) to set it up

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