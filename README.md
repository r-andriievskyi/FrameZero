# FrameZero

Production management for film & TV crews — schedules, tasks, and the people behind them.

Android, iOS, and the backend all run on one Kotlin codebase.

## Stack

- **Apps** — Compose Multiplatform UI on Android and iOS, navigation with Decompose
- **Shared** — Kotlin Multiplatform business logic; offline-first via Room + Paging 3
- **Server** — Ktor and Postgres, JWT auth

## Getting started

Start the backend:

```bash
docker compose up -d          # Postgres
./gradlew :server:run         # Ktor on :8080
./scripts/seed_db.sh          # demo data
```

Run the Android app:

```bash
./gradlew :composeApp:installDebug
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and hit Run.

## Project layout

| Module | What's inside |
|--------|---------------|
| `composeApp/` | Compose UI for Android & iOS |
| `shared/` | Shared logic, models, networking, repositories |
| `server/` | Ktor backend |
| `iosApp/` | SwiftUI host |
