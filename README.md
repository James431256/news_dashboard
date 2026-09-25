# News Intelligence Dashboard 📰⚡

A robust, enterprise-grade News Intelligence Dashboard built with **Java 17** and **Spring Boot 3**. The application aggregates real-time news articles from multiple independent providers, runs heuristic sentiment analysis, and presents the intelligence on a clean, responsive web interface.

Built for the **Graduate Engineering Technical Assessment**.

---

## 🚀 Key Features

- **Multi-Provider Aggregation:** Integrates multiple news services (The Guardian Open Platform, Hacker News / Algolia, and GNews) under a unified domain model.
- **Asynchronous Parallel Fetching:** Leverages Java's `CompletableFuture` to query external APIs concurrently, significantly reducing total response latency.
- **News Intelligence & Sentiment Analysis:** Evaluates headline and summary tokens to score and classify each article into `POSITIVE`, `NEUTRAL`, or `NEGATIVE`, compiling an executive sentiment breakdown.
- **Fault-Tolerant & Resilient:** Individual provider timeouts, graceful error catching, and fallbacks ensure that a failure or rate-limit on one provider never crashes the application.
- **Interactive Web Dashboard:**
  - Keyword and category searching (Technology, Business, Science, Politics, General).
  - In-place sentiment filtering without server round-trips.
  - Recent searches history saved to `localStorage`.
  - Article bookmarking / favourites system.
- **Defensive API Design:** Centralized `@RestControllerAdvice` error handling, input validation, and clean REST JSON contracts.

---

## 🏛️ System Architecture & Design Patterns

```
                                  ┌─────────────────────────┐
                                  │   Frontend Dashboard    │
                                  │ (HTML5 / CSS3 / Vanilla)│
                                  └────────────┬────────────┘
                                               │ HTTP / REST
                                               ▼
                                  ┌─────────────────────────┐
                                  │     NewsController      │
                                  │ (Input Validation / DTO)│
                                  └────────────┬────────────┘
                                               │
                                               ▼
                                  ┌─────────────────────────┐
                                  │  NewsAggregatorService  │
                                  │ (CompletableFuture Pool)│
                                  └─────┬──────────────┬────┘
                                        │              │
                       Enriches with    │              │ Queries in parallel
                                        ▼              ▼
                     ┌───────────────────────┐   ┌───────────────────────────┐
                     │SentimentAnalysisEngine│   │   <<interface>> NewsClient│
                     └───────────────────────┘   └─────────────┬─────────────┘
                                                               │
                                       ┌───────────────────────┼───────────────────────┐
                                       │                       │                       │
                                       ▼                       ▼                       ▼
                            ┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
                            │ GuardianNewsClient  │ │  HackerNewsClient   │ │    GNewsClient      │
                            │ (The Guardian API)  │ │   (Algolia Open)    │ │   (GNews.io API)    │
                            └─────────────────────┘ └─────────────────────┘ └─────────────────────┘
```

### 1. Strategy & Adapter Pattern (`NewsClient.java`)
Adheres to the **Open-Closed Principle (SOLID)**. The core business logic interacts exclusively with the `NewsClient` interface. Each external API has its own adapter that transforms provider-specific JSON into our unified `Article` domain model. Adding a new provider requires writing a new class without modifying existing code.

### 2. Parallel Concurrency (`CompletableFuture`)
Rather than sequentially blocking on external network calls ($T_{total} = T_1 + T_2$), providers are queried concurrently in parallel threads ($T_{total} = \max(T_1, T_2)$).

### 3. Separation of Concerns
- **Backend:** Pure REST API providing structured JSON contracts.
- **Frontend:** Lightweight, decoupled client served from Spring Boot's static resources. Zero node/npm compilation overhead during deployment.

---

## 🛠️ Technology Stack

- **Backend:** Java 17 LTS, Spring Boot 3.3.4
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-test`
- **Testing:** JUnit 5, Mockito, Spring MockMvc
- **Build Tool:** Apache Maven (configured via Maven Wrapper `mvnw`)
- **Frontend:** Responsive HTML5, CSS Grid/Flexbox, Vanilla JavaScript (ES6+), FontAwesome

---

## 🏃 Getting Started

### Prerequisites
- **Java 17** or higher installed (`java -version`)
- Internet connection for fetching live news feeds

### Run the Application
From the project root directory:

**Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

Once started, open your browser and navigate to:
```
http://localhost:8080
```

---

## 🧪 Running Automated Tests

A comprehensive suite of unit and slice tests verifies the controller, service aggregation, and sentiment scoring:

```powershell
.\mvnw.cmd test
```

Test coverage includes:
1. `SentimentAnalysisServiceTest`: Verifies positive, negative, and neutral lexical scoring.
2. `NewsAggregatorServiceTest`: Mocks client adapters to test concurrent aggregation and sentiment enrichment.
3. `NewsControllerTest`: Uses `MockMvc` to verify HTTP 200 responses for valid queries and HTTP 400 Bad Request handling for empty queries.

---

## ⚙️ Configuration & API Keys

The application works out of the box with the public open news feed. To enable your personal keys for The Guardian or GNews, configure `src/main/resources/application.properties`:

```properties
# Register free developer keys:
# The Guardian: https://open-platform.theguardian.com/access/
# GNews: https://gnews.io/
guardian.api.key=YOUR_GUARDIAN_API_KEY
gnews.api.key=YOUR_GNEWS_API_KEY
```

---

## 💡 Decisions & Engineering Trade-Offs

| Decision | Selected Approach | Trade-Off Considered |
| :--- | :--- | :--- |
| **Frontend Framework** | Vanilla JS + HTML5/CSS | *Trade-off:* While React/Vue offers component libraries, Vanilla JS avoids heavy build pipelines, npm version conflicts, and ensures an instant, reliable single-command demo. |
| **Sentiment Analysis** | Lexicon / Rule-based Heuristic | *Trade-off:* Avoids heavy Python/PyTorch dependencies or paid cloud AI API latency while remaining fast, deterministic, explainable, and zero-cost. |
| **Client Storage** | LocalStorage for Bookmarks | *Trade-off:* Relational DB (e.g. H2/PostgreSQL) requires authentication/sessions; LocalStorage provides instant persistence per browser with zero setup. |

---

## 🔮 Future Extensions

If extending this application further in a production environment:
1. **Caching Layer (Redis / Caffeine):** Cache query responses with a 10-minute TTL to reduce third-party API rate-limit consumption and improve response times.
2. **User Accounts & Cloud Sync:** Introduce Spring Security with JWT to allow users to sync their bookmarked articles and custom topic feeds across devices.
3. **Advanced NLP Pipeline:** Integrate an ONNX runtime or HuggingFace sentiment model (e.g. FinBERT) directly in Java for deeper contextual sentiment analysis.
4. **Containerization:** Package with a multi-stage `Dockerfile` and `docker-compose.yml` for automated CI/CD deployment to Kubernetes or cloud providers.
