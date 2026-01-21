# How to run Liquibase scripts locally

## Integration Tests

The integration tests automatically run the Liquibase migrations against an in-memory database.

## Rollback command from Terminal
./gradlew rollbackCount -PliquibaseCommandValue=1
