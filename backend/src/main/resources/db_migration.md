# Database Migration Guide

All commands must be run from `backend/src/main/resources/`.

## Prerequisites (first time only)

```bash
# 1. Create the database
psql -h localhost -p 54320 -U admin -c "CREATE DATABASE sugar_advisor;"

# 2. Create the schema
psql -h localhost -p 54320 -U admin -d sugar_advisor -c "CREATE SCHEMA IF NOT EXISTS sugar_advisor;"
```

## Apply

Apply all pending migrations:
```bash
liquibase --defaults-file=liquibase.properties update
```

## Rollback

Rollback the last N changesets (changesets must have `<rollback>` blocks):
```bash
liquibase --defaults-file=liquibase.properties rollbackCount 1
```

Rollback to a specific tag:
```bash
liquibase --defaults-file=liquibase.properties rollback <tag>
```

## Status

Check which changesets are pending (dry run):
```bash
liquibase --defaults-file=liquibase.properties status
```

View full changelog history:
```bash
liquibase --defaults-file=liquibase.properties history
```
