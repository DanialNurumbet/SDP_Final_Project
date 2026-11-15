# SportRent

A web application for tracking the rental of sports equipment (an educational project focused on design patterns).
Java 21, Spring Boot 3.5.0, Maven Wrapper. User interface in Russian, currency: KZT

## Launch
```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
Open http://localhost:8080
## Build and tests
```bash
./mvnw clean verify
```

## Status
Framework: common models, DTOs, interfaces, error format, main page. Business modules (catalog, storage, pricing, rental, reporting) are to be implemented by participants. See `docs/contracts.md` for details.
