# Earthquake Monitor — CodeIt

Web application that fetches, filters, stores and displays real-time earthquake data from the USGS API.

---

## Setup

**Prerequisites:** Java 17+, Maven, Node.js, PostgreSQL

### Database

**Option A — Docker**

```bash
docker-compose up -d
```

**Option B — Local PostgreSQL**

Create a database called `earthquake_db` and update `application.properties` if your credentials differ:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/earthquake_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

The table is created automatically on first startup.

### Backend

```bash
cd backend
mvn spring-boot:run
```

Runs on `http://localhost:8080`. Swagger UI available at `/swagger-ui/index.html`.

### Frontend

```bash
cd frontend
npm install
npm start
```

Runs on `http://localhost:3000`.

### Tests

```bash
mvn test
```

---

## Assumptions

- Only earthquakes with magnitude > 2.0 are stored. This is configurable in `application.properties`.
- On every fetch, existing records are cleared before inserting new ones to avoid duplicates.
- The USGS endpoint only returns data from the last hour, so results may be empty during low seismic activity.

---

## Additional Features

- Map view with Leaflet (markers colored by magnitude)
- Stats bar with total count, max/avg magnitude and M5.0+ events
- Delete individual records from the UI
- Swagger UI for API documentation
- Factory Method pattern for parsing GeoJSON features