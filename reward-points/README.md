# Reward Points REST API

A Java 17 / Spring Boot REST API that calculates reward points per customer per calendar month and totals them across an inclusive date range.

## Reward rules

- $0 through $50: 0 points.
- Above $50 through $100: 1 point per whole dollar above $50.
- Above $100: 50 points for the $50-$100 tier, plus 2 points per whole dollar above $100.
- Fractional dollars are ignored. For example, $75.99 earns 25 points and $120 earns 90 points.

## Endpoint

`GET /api/rewards?startDate=2026-01-01&endDate=2026-03-31`

Both parameters are required, inclusive, and formatted as `yyyy-MM-dd`. Months are derived from transaction dates using `YearMonth`; the application does not hard-code month names or assume a particular three-month period. Only months with transactions in the range are returned. Customers with no transactions in the range are omitted.

Example response:

```json
[
  {
    "customerId": 1001,
    "monthlyRewards": [
      { "month": "2026-01", "points": 115 },
      { "month": "2026-02", "points": 250 },
      { "month": "2026-03", "points": 0 }
    ],
    "totalPoints": 365
  }
]
```

## Run locally

Requirements: JDK 17+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

H2 is an in-memory database. `schema.sql` creates the table and index; `data.sql` inserts sample transactions for multiple customers across several months.

## Run tests

```bash
mvn test
```

Unit tests cover reward boundaries, the example calculation, fractional dollars, invalid amounts, multiple customers, multiple transactions, monthly aggregation, and reversed dates. Integration tests exercise the real REST controller and H2 database, including successful requests, malformed dates, missing parameters, and reversed ranges.

## Project structure

```text
reward-points/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/example/rewards/
    │   │   ├── RewardPointsApplication.java
    │   │   ├── controller/ (REST controller and exception handler)
    │   │   ├── exception/ (date range exception)
    │   │   ├── model/ (transaction and response records)
    │   │   ├── repository/ (repository interface and JDBC implementation)
    │   │   └── service/ (reward rules and aggregation)
    │   └── resources/ (application.properties, schema.sql, data.sql)
    └── test/java/com/example/rewards/
        ├── service/RewardCalculationServiceTest.java
        └── controller/RewardControllerIntegrationTest.java
```

## Implementation choices

- Layered structure: controller, service, repository, models, and exception handling.
- Constructor-based dependency injection and explicit imports (no wildcard imports).
- `BigDecimal` for currency and `YearMonth` for dynamic monthly grouping.
- SQL scripts provide schema and seed data; sample data is not embedded in Java classes.
- JavaDoc is included for public classes and methods.
- `.gitignore` excludes `target/` and `bin/`; do not commit build output.

For production, use a managed database, externalized credentials, Flyway/Liquibase migrations, and appropriate authentication/authorization.
