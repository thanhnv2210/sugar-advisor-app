# Redis Serialization Fails on LocalDateTime

## Symptom

Product endpoint returns 500 with:

```json
{
  "status": 500,
  "detail": "Could not write JSON: Java 8 date/time type `java.time.LocalDateTime` not supported by default:
             add Module \"com.fasterxml.jackson.datatype:jackson-datatype-jsr310\" to enable handling
             (through reference chain: com.sugaradvisor.domain.Product[\"createdAt\"])"
}
```

## Cause

`Jackson2JsonRedisSerializer` creates its own internal `ObjectMapper` that does not have `JavaTimeModule` registered. When Spring Data R2DBC maps the `created_at` DB column to a `LocalDateTime` field on the entity, the Redis serializer can't convert it to JSON.

Attempts to fix by passing a custom `ObjectMapper` or using `@JsonIgnore` / `@JsonIgnoreProperties` do not reliably work due to how `Jackson2JsonRedisSerializer` handles the provided mapper internally.

## Fix

Remove the `LocalDateTime` field from the entity if it is not used in application logic. R2DBC silently ignores DB columns that have no matching Java field — the column still exists in the database, the entity just doesn't map it.

**Before (`Product.java`):**
```java
@Column("created_at")
private LocalDateTime createdAt;
```

**After:** Delete the field entirely. The `created_at` column remains in the DB and is still populated by `DEFAULT NOW()`.

## Rule

Do not map timestamp-only audit columns (`created_at`, `updated_at`) on entities that are cached in Redis unless you configure a Redis `ObjectMapper` with `JavaTimeModule` explicitly. Keep Redis-cached entities simple and free of Java date/time types, or use `String` / `Instant` (which serializes as a numeric epoch and requires no extra module).
