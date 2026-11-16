# SportRent

Web application for tracking sports equipment rentals (a university project on software design patterns).
Java 21, Spring Boot 3.5.0, Maven Wrapper. User interface language: Russian. Currency: KZT.

## Requirements
- JDK 21 or newer (the project targets Java 21).
- No separate Maven install is needed: the Maven Wrapper (`mvnw`) downloads it.

## Run
```bash
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```
Wait for the line `Started SportRentApplication`, then open **http://localhost:8080** in the browser.

> Do not use the IDE's "Open in Browser" on an HTML file. It opens the page on another port (for example 63342),
> where the Java backend does not exist, so every `/api/...` request returns 404 and pages show
> "Каталог не загружен" or an empty history. Always use `http://localhost:8080`.

## Pages
| Page | Purpose |
|---|---|
| `/index.html` | Home |
| `/catalog.html` | Equipment catalog: create, edit, delete, filters |
| `/pricing.html` | Price calculator: tariff and extra services |
| `/rentals.html` | Create rentals, active rentals, returns |
| `/history.html` | Rental history, details, summary, TXT report download |

## Data
Data is stored in `data/sportrent.json`. On the first run, when the file does not exist, nine demo items
are created (three bicycles, three balls, three rackets). To reset the demo data, stop the application
and delete this file

## Build and test
```bash
./mvnw clean verify
```
Runs all unit and API tests and builds `target/sportrent-0.1.0-SNAPSHOT.jar`.
Run the built jar with `java -jar target/sportrent-0.1.0-SNAPSHOT.jar`.

## Documentation
- `docs/contracts.md`: shared models, interfaces, DTOs, errors and REST routes.
- `docs/member-1.md` ... `docs/member-4.md`: work of each team member.
- `docs/patterns.md`: all patterns in one table.
- `docs/test-scenarios.md`: test scenarios and results.
- `docs/diagrams/`: PlantUML sources and PNG renders (class diagram, rental creation sequence).
