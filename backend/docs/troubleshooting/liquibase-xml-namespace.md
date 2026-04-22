# Liquibase XML Namespace Declaration

## Symptom

```
Unexpected error running Liquibase: Error parsing line 1 column 72 of db/changelog/changes/002-users.xml:
cvc-elt.1.a: Cannot find the declaration of element 'databaseChangeLog'.
```

## Cause

Each individual changelog file must include the full XML namespace declaration with `xsi:schemaLocation`. A minimal `xmlns` without the schema location fails strict XML validation in Liquibase 4.x.

**Broken:**
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog">
```

**Fixed:**
```xml
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.3.xsd">
```

## Fix

Add the full namespace block to every `<databaseChangeLog>` element in every changeset file, including seed files. The master changelog (`db.changelog-master.xml`) typically has it correct — the individual `changes/*.xml` files often do not.
