# Gradle & Spring Boot Version Compatibility

## Symptom

```
Execution failed for task ':compileJava'.
> Failed to notify dependency resolution listener.
   > 'java.util.Set org.gradle.api.artifacts.LenientConfiguration.getArtifacts(...)'
```

## Cause

Spring Boot 3.2.x is not compatible with Gradle 9.x. The Gradle API changed in 9.x in a way that breaks the Spring Boot Gradle plugin.

## Fix

Generate the Gradle wrapper pinned to version 8.8:

```bash
gradle wrapper --gradle-version 8.8
```

Then use `./gradlew` instead of `gradle` for all commands.

## Compatibility Reference

| Spring Boot | Gradle |
|---|---|
| 3.2.x | 7.6.x – 8.x |
| 3.3.x | 7.6.x – 8.x |
| 3.4.x+ | 8.x – 9.x |
