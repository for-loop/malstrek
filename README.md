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
```

## Run

```bash
docker compose up -d
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