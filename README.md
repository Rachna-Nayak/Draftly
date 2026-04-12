# Draftly

Draftly is an academic research assistance platform with:
- A Spring Boot backend (Java 21, Gradle)
- A React frontend (Vite)
- MongoDB Atlas for persistence

## Tech Stack

- Backend: Spring Boot 4, Spring Data MongoDB, Spring Validation, Apache OpenNLP
- Frontend: React 19, Vite 7, React Router
- Database: MongoDB Atlas
- Build tools: Gradle (backend), npm (frontend)

## Prerequisites

- Java 21
- Node.js 20+ and npm
- MongoDB Atlas cluster + connection string

## Environment Setup

1. Configure backend properties in `src/main/resources/application.properties`.
2. Set your MongoDB URI and database name.
3. (Optional) Copy `.env.example` to `.env.local` and keep local values there for reference.

Example MongoDB config:

```properties
spring.application.name=draftly
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>/<database>?retryWrites=true&w=majority
spring.data.mongodb.database=draftly
server.port=8080
```

If you need step-by-step Atlas setup, see `MONGODB_ATLAS_SETUP.md`.

## Role-Based Access Control (RBAC)

Draftly now uses three normalized roles:

- `AUTHOR`
- `REVIEWER`
- `ADMIN`

Legacy role values are still accepted and mapped automatically:

- `STUDENT_RESEARCHER` → `AUTHOR`
- `FACULTY_SUPERVISOR` → `REVIEWER`
- `SYSTEM_ADMINISTRATOR` → `ADMIN`

### Access Matrix

- **AUTHOR**
	- Create/manage projects, papers, references, and submissions
	- Use search, credibility, plagiarism, export, and metrics views
	- Cannot access reviewer assignment, analytics admin logs, or session admin endpoints

- **REVIEWER**
	- Access review queue, review schedules, review/feedback operations
	- Access notifications and read-only project/submission/search/export views
	- Cannot perform admin-only management actions

- **ADMIN**
	- Full platform access, including reviewer assignment, analytics/logs, conference/session management

## Running the App (Development)

Run backend and frontend in separate terminals.

### 1) Start backend

```bash
./gradlew bootRun
```
Backend starts on `http://localhost:8080` by default.

### 2) Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on `http://localhost:3000`.

The frontend uses `/api` as base path, so configure Vite proxy (if needed) to forward `/api` to the backend.

## Build

### Backend

```bash
./gradlew clean build
```

### Frontend

```bash
cd frontend
npm run build
```

## Test

Run backend tests:

```bash
./gradlew test
```

