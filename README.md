<h1 align="center">Sleep Logger API</h1>
<p align="center">A REST API for logging sleep data and viewing sleep averages.</p>

<p align="center">
  <!-- build -->
  <img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/f-ernanda/sleep-logger-api/ci.yml">
  <!-- languages -->
  <img alt="GitHub language count" src="https://img.shields.io/github/languages/count/f-ernanda/sleep-logger-api?color=CB504C">
  <!-- top language-->
  <img alt="Top language" src="https://img.shields.io/github/languages/top/f-ernanda/sleep-logger-api?color=cb744c">
</p>

<p align="center">
  <a href="#-about-the-project">About the project</a> •
  <a href="#-features">Features</a> •
  <a href="#️-technologies">Technologies</a> •
  <a href="#-how-to-run">How to run</a> •
  <a href="#-testing">Testing</a> •
  <a href="#-api">API</a> •
  <a href="#-documentation">Documentation</a>
</p>

## 💻 About the project

Sleep Logger is a REST API that records sleep data and generates statistics based on the user's sleep history. The API supports recording sleep logs, retrieving the most recent entry, and calculating averages over the previous 30 days.

The project was originally developed as a backend take-home assignment for [Noom](https://www.noom.com/). The original assignment brief is preserved in [docs/assignment.md](./docs/assignment.md).

## ✨ Features

- Log sleep data and morning feeling
- Retrieve the most recent sleep log
- Calculate 30-day sleep averages

## 🛠️ Technologies

- Kotlin
- Spring Boot
- PostgreSQL
- Docker

## 🚀 How to run

### Prerequisites

Before running the project, make sure you have:

- Docker
- Docker Compose

No local JDK, Gradle, or PostgreSQL installation is required.

### Installation

Clone the repository:

```bash
git clone git@github.com:f-ernanda/sleep-logger-api.git
cd sleep-logger-api
```

### Running

Start the services with:

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080` and PostgreSQL at `localhost:5432`.

To stop the services:

```bash
docker compose down
```

## 🧪 Testing

Run the automated tests with:

```bash
docker compose run --rm -T -v /var/run/docker.sock:/var/run/docker.sock sleep_api ./gradlew test
```

You can also run the end-to-end tests while the application is running:

```bash
./scripts/smoke-test.sh
```

See [docs/testing.md](docs/testing.md) for the full strategy.

## 🧩 API

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/users/{userId}/sleep-logs` | Log the previous night's sleep |
| GET | `/users/{userId}/sleep-logs/latest` | Retrieve the most recent sleep log |
| GET | `/users/{userId}/sleep-logs/averages` | Retrieve 30-day sleep averages |

See the [API documentation](./docs/api.md) for details about request and response contracts.

## 📚 Documentation

- [Requirements](./docs/requirements.md) — functional and non-functional requirements
- [Data model](./docs/data-model.md) — database structure
- [API](./docs/api.md) — REST API contracts
- [Testing](./docs/testing.md) — testing strategy
- [Decisions](./docs/decisions.md) — relevant technical decisions
- [Original assignment](./docs/assignment.md) — original technical assignment

---

🌱 Crafted by [Fernanda](https://github.com/f-ernanda)
