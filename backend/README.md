# Sugar Advisor — Backend

Spring Boot WebFlux API serving `http://localhost:8080/api`.

## Prerequisites

- Java 17
- Docker (for Redis)
- PostgreSQL instance running on port `54320` with database `sugar_advisor` and schema `sugar_advisor`

## Start

**1. Start Redis**
```bash
docker-compose -f ../docker-compose.yml up -d redis
```

**2. Start the API**
```bash
./gradlew bootRun
```

The app runs Liquibase migrations automatically on startup. Ready when you see:
```
Netty started on port 8080
Started SugarAdvisorApplication
```

## Stop

```bash
# Stop the API (Ctrl+C if in foreground, or:)
lsof -ti:8080 | xargs kill -9

# Stop Redis
docker stop sugar-advisor-redis
```

## Build

```bash
# Compile and package (skip tests)
./gradlew clean build -x test

# Run tests
./gradlew test

# Single test class
./gradlew test --tests "com.sugaradvisor.<ClassName>"
```

## Database Migrations

See `src/main/resources/db_migration.md` for full Liquibase CLI reference.

```bash
# Run from src/main/resources/
cd src/main/resources

# Apply pending migrations
liquibase --defaults-file=liquibase.properties update

# Rollback last changeset
liquibase --defaults-file=liquibase.properties rollbackCount 1

# Check status
liquibase --defaults-file=liquibase.properties status
```

## Logs

**Foreground** (`./gradlew bootRun`) — logs stream directly to the terminal.

**Background** (started with `nohup ... &`) — logs go to `/tmp/sugar-backend.log`:

```bash
# Follow live
tail -f /tmp/sugar-backend.log

# Last 100 lines
tail -100 /tmp/sugar-backend.log

# Errors only
grep ERROR /tmp/sugar-backend.log

# Filter by class/keyword
grep "ProductService" /tmp/sugar-backend.log
```

To start in background and redirect logs:
```bash
nohup ./gradlew bootRun > /tmp/sugar-backend.log 2>&1 &
```

## Health Check

```bash
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

## Swagger UI

Open in browser after starting the app:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Environment Variables

All have sensible defaults for local development.

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `54320` | PostgreSQL port |
| `DB_NAME` | `sugar_advisor` | Database name |
| `DB_USER` | `admin` | Database user |
| `DB_PASSWORD` | `admin` | Database password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |

Override at runtime:
```bash
DB_HOST=prod-host DB_PORT=5432 ./gradlew bootRun
```
