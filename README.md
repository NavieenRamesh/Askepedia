# Askepedia 🧠🔍

> **A database-first, AI-powered Wikipedia research and search engine.**  
> Built with **Java 21 / Spring Boot 3**, **PostgreSQL (pgvector)**, and **local AI models via Ollama**.

---

## 🌟 Overview

Askepedia allows users to ask questions in natural language and receive concise, evidence-grounded answers backed by locally stored Wikipedia passages with clickable source citations.

The core engineering objective is building a reliable, production-grade application combining traditional database retrieval (full-text search) with modern AI retrieval-augmented generation (RAG) — completely running on local hardware.

Even when the local AI model is offline, Askepedia remains fully functional for standard article retrieval and keyword search.

---

## 🏗️ Architecture

```
[ React Web Interface ] (Phase 11)
         │
         ▼
[ Spring Boot 3 REST API ]
  ├── Controllers  (REST endpoints: /api/health, /api/articles, /api/search, /api/ask)
  ├── Services     (Wikipedia ingestion, passage chunking, prompt orchestration)
  ├── DAOs         (Data Access Objects via JDBC / Spring Data)
  └── DTOs         (Input validation and structured responses)
         │
         ├───► [ PostgreSQL + pgvector ] (Articles, Passages, tsvector, Embeddings)
         ├───► [ Ollama Engine ]         (Local LLM generation & nomic-embed-text)
         └───► [ Wikipedia API ]         (Raw article fetching & source metadata)
```

### Key Workflows:
1. **Ingestion Workflow:** Wikipedia API → Clean text → Normalized passages → PostgreSQL → Vector embeddings.
2. **Question Workflow:** User Query → Hybrid retrieval (Keyword + Semantic) → Grounded context assembly → Local LLM → Answer with verified citations.

---

## 📂 Project Structure

```
Askepedia/
├── backend/                  # Java 21 + Spring Boot 3 backend service
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/askepedia/
│       │   │   ├── AskepediaApplication.java
│       │   │   ├── controller/     # REST Endpoints (@RestController)
│       │   │   ├── dto/            # Data Transfer Objects
│       │   │   ├── entity/         # Database domain entities
│       │   │   ├── dao/            # Data Access Objects (@Repository)
│       │   │   ├── service/        # Core business & RAG orchestration logic
│       │   │   └── config/         # Spring beans & configuration
│       │   └── resources/
│       │       ├── application.yml # Application configuration
│       │       └── db/migration/   # Flyway database migrations (Phase 3+)
│       └── test/                   # Automated unit & integration tests
├── docs/                     # Architecture & evaluation documentation
├── scripts/                  # Helper & setup scripts
├── .env.example              # Environment variables template
├── .gitignore
├── LICENSE                   # MIT License
└── README.md
```

---

## 🚀 Quickstart (Phase 1)

### Prerequisites
- **Java:** JDK 21+ (tested on OpenJDK 25 LTS)
- **Maven:** 3.9+
- **Git:** 2.x+

### 1. Clone the repository
```bash
git clone https://github.com/NavieenRamesh/Askepedia.git
cd Askepedia
```

### 2. Build and run tests
```bash
mvn clean test -f backend/pom.xml
```

### 3. Run the backend service
```bash
mvn spring-boot:run -f backend/pom.xml
```

The application starts on `http://localhost:8080`.

### 4. Verify health endpoint
```bash
curl http://localhost:8080/api/health
```
Or in PowerShell:
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/health
```

Expected Response:
```json
{
  "status": "UP",
  "service": "Askepedia API",
  "version": "0.1.0-SNAPSHOT",
  "timestamp": "2026-10-25T13:28:00Z"
}
```

---

## 🗺️ 12-Week Roadmap Tracking

- [x] **Week 1 (Phase 1): Project Setup and GitHub** — Spring Boot app, health endpoint, tests, docs, and git setup.
- [ ] **Week 2 (Phase 2): Wikipedia Integration** — Fetch articles, parse metadata, error handling, DTOs.
- [ ] **Week 3 (Phase 3): PostgreSQL Integration & Schema** — Flyway migrations, article & passage entities, JDBC DAOs.
- [ ] **Week 4 (Phase 4): Article Ingestion & Passage Splitting** — Chunking algorithm, idempotency, batch import.
- [ ] **Week 5 (Phase 5): PostgreSQL Full-Text Search** — `tsvector`, `websearch_to_tsquery`, ranking, query optimization.
- [ ] **Week 6 (Phase 6): Local Language Model Integration** — Ollama client, prompt templates, streaming responses.
- [ ] **Week 7 (Phase 7): Evidence-Grounded RAG Pipeline** — Context injection, hallucination prevention, source citations.
- [ ] **Week 8 (Phase 8): RAG Reliability & Testing** — Insufficient evidence handling, evaluation question set.
- [ ] **Week 9 (Phase 9): Embedding Generation** — Local embedding model (`nomic-embed-text`), batch vector generation.
- [ ] **Week 10 (Phase 10): Semantic & Hybrid Search** — `pgvector` indexing, reciprocal rank fusion (RRF).
- [ ] **Week 11 (Phase 11): React Frontend & Usability** — Search UI, citation links, interactive evidence explorer.
- [ ] **Week 12 (Phase 12): Performance, Documentation & Release** — Benchmarks, evaluation report, final release.

---

## 👤 Author

Developed by **[Navieen Ramesh](https://github.com/NavieenRamesh)**.
License: [MIT](LICENSE)
