# FrameZero command runner — one discoverable home for the project's non-obvious
# commands (see docs/platform-productivity-audit.md §3.2).
#
# Install just:  brew install just   (https://github.com/casey/just)
# List recipes:  just   (or `just --list`)

# Default recipe: show the available commands.
default:
    @just --list

# ---------------------------------------------------------------------------
# Backend
# ---------------------------------------------------------------------------

# Start the Postgres container (detached).
db-up:
    docker compose up -d

# Stop the Postgres container (keeps the volume/data).
db-down:
    docker compose down

# Stop the Postgres container AND delete its data volume.
db-reset:
    docker compose down -v

# Run the Ktor server on :8080. Needs FIREBASE_CREDENTIALS_PATH set; prod mode
# (KTOR_ENV=production) also needs JWT_SECRET + DB vars.
server:
    ./gradlew :server:run

# Seed the running server with demo data (users, productions, tasks, events).
# Optional origin arg, e.g. `just seed http://localhost:8080`.
seed origin="http://localhost:8080":
    ./scripts/seed_db.sh {{origin}}

# Bring the whole backend up from cold: Postgres, then the server.
backend: db-up server

# ---------------------------------------------------------------------------
# Android
# ---------------------------------------------------------------------------

# Build and install the Android debug app on a connected device/emulator.
android:
    ./gradlew :composeApp:installDebug

# ---------------------------------------------------------------------------
# iOS
# ---------------------------------------------------------------------------

# Relink the shared debug framework for the iOS simulator. Run after changing
# `shared`/`composeApp`, then Run from Xcode (iosApp/iosApp.xcodeproj).
ios-link:
    ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# ---------------------------------------------------------------------------
# Code quality
# ---------------------------------------------------------------------------

# Auto-format all Kotlin source (run before committing).
format:
    ./gradlew ktlintFormat

# Verify Kotlin formatting (as CI does).
lint:
    ./gradlew ktlintCheck

# Static analysis.
detekt:
    ./gradlew detekt

# Full CI gate: ktlintCheck + detekt + tests.
check:
    ./gradlew check

# ---------------------------------------------------------------------------
# Screenshot tests (Roborazzi) — `check` does NOT run these; invoke explicitly.
# ---------------------------------------------------------------------------

# Verify all screenshot goldens.
screenshots-verify:
    ./gradlew verifyRoborazziAndroidHostTest

# Re-record all screenshot goldens (after adding/removing previews or UI changes).
screenshots-record:
    ./gradlew recordRoborazziAndroidHostTest

# ---------------------------------------------------------------------------
# Docs
# ---------------------------------------------------------------------------

# Regenerate docs/module-graph.md (Mermaid module dependency graph).
module-graph:
    ./gradlew createModuleGraph

