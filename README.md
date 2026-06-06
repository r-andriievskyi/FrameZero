# FrameZero

A cross-platform app for managing film & TV productions — schedules, tasks, and crew — built with Kotlin Multiplatform.

One Kotlin codebase powers the Android and iOS apps and the backend.

## Stack

- **Apps** — Compose Multiplatform UI (Android + iOS), Decompose navigation
- **Shared** — Kotlin Multiplatform business logic, offline-first with Room + Paging 3
- **Server** — Ktor + Postgres, JWT auth

## Getting started

```bash
# Backend (Postgres + Ktor on :8080)
docker compose up -d
./gradlew :server:run
./scripts/seed_db.sh          # seed demo data

# Android
./gradlew :composeApp:installDebug
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run.

## Layout

| Module | What's inside |
|--------|---------------|
| `composeApp/` | Compose UI for Android & iOS |
| `shared/` | Shared logic, models, networking, repositories |
| `server/` | Ktor backend |
| `iosApp/` | SwiftUI wrapper |
