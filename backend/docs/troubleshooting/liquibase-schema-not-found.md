# Liquibase Schema Does Not Exist

## Symptom

```
Unexpected error running Liquibase:
ERROR: schema "sugar_advisor" does not exist
  [Failed SQL: CREATE TABLE sugar_advisor.databasechangeloglock ...]
```

## Cause

Liquibase creates its own tracking tables (`databasechangeloglock`, `databasechangelog`) in the configured `defaultSchemaName`. If that schema doesn't exist yet, Liquibase fails before it can run any migrations — including the migration that would create the schema.

## Fix

Create the schema manually before the first Liquibase run:

```bash
psql -h localhost -p 54320 -U admin -d sugar_advisor \
     -c "CREATE SCHEMA IF NOT EXISTS sugar_advisor;"
```

This is a one-time setup step. After the schema exists, subsequent `liquibase update` and application startups work without any manual intervention.

## Note

This only affects the first-time setup. The `001-init-schema.xml` changeset also creates the schema via SQL, but Liquibase needs the schema to already exist in order to track that changeset.
