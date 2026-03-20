# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development (live reload)
./mvnw quarkus:dev

# Build
./mvnw package

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=MyTestClass

# Native build (requires GraalVM)
./mvnw package -Dnative

# Native build via Docker (no GraalVM required locally)
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

The app runs on `http://localhost:8080` by default. PostgreSQL must be running (Quarkus Dev Services will auto-start a container in dev mode).

## Architecture

This is a real-time AI-powered log analysis platform built on Quarkus. It simulates distributed system failures, analyzes them with LLMs, and streams results to a browser dashboard.

### Data Flow

1. `ChaosService` (scheduled every 1.5s) calls `FailureGeneratorAgent` to generate a realistic error log line
2. The generated log is passed to `LogAnalyzerAgent` for structured analysis → returns `LogAnalysis` entity
3. `LogAnalysis` is persisted to PostgreSQL via Hibernate Panache
4. A CDI event (`LogAnalysisPersistedEvent`) is fired
5. `LogAnalysisBroadcasterService` observes the event and broadcasts a `LogAnalysisResult` record over WebSocket
6. Connected browser clients receive JSON in real-time; initial load fetches history via `GET /api/logs`

### AI Agents (LangChain4j + Ollama)

Three `@RegisterAiService` interfaces, each backed by a named Ollama model configured in `application.properties`:

| Bean name | Interface | Role | Temperature |
|---|---|---|---|
| `demo-generator` | `FailureGeneratorAgent` | Generates realistic Prometheus-style error log lines | 0.8 |
| `demo-analyzer` | `LogAnalyzerAgent` | Parses raw logs into structured JSON (severity, component, errorType, rootCauseSummary, suggestedAction) | 0.0 |
| `demo-llama` | `SystemHealthAgent` | Produces markdown health reports from aggregated stats | default |

Prompts use `@SystemMessage` / `@UserMessage` with `{{variable}}` template syntax. The analyzer agent must return a strict JSON structure matching `LogAnalysis` fields.

### Key Packages

- `com.eldermoraes.ai` — AI agent interfaces (`@RegisterAiService`)
- `com.eldermoraes.model` — `LogAnalysis` (JPA entity + Panache), `LogAnalysisResult` (WebSocket record), `Severity` enum
- `com.eldermoraes.service` — `ChaosService` (scheduler), `LogAnalysisBroadcasterService` (CDI event observer)
- `com.eldermoraes.resource` — REST endpoints (`/api/logs`, `/health-report`)
- `com.eldermoraes.ui` — WebSocket endpoint (`/logs/stream`), static UI file serving

### REST & WebSocket API

| Method | Path | Description |
|---|---|---|
| GET | `/api/logs` | All persisted log analyses (JSON array) |
| GET | `/health-report?pageSize=N` | Markdown health report (pageSize: 10–500, default 50) |
| WS | `/logs/stream` | Real-time log broadcast |
| GET | `/` | Dashboard UI |
| GET | `/report` | Health report UI |

### Frontend

Vanilla JS/HTML/CSS (no frameworks) in `src/main/resources/ui/`. The dashboard auto-reconnects WebSocket every 3s, supports severity filtering, pause/resume, and auto-scroll.

### Configuration

All LLM model bindings, timeouts, and database settings live in `src/main/resources/application.properties`. Named Ollama configs follow the pattern `quarkus.langchain4j.ollama.<name>.*`.
