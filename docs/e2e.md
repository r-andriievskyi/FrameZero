# End-to-end tests (Maestro)

Black-box UI flows that drive the **demo flavor** — fully offline, `DemoDataStore` seed
data, `DemoAuthRepository` accepts any credentials. One flow set (`appId: com.frame.zero`)
serves both Android and iOS.

Flows live in [`.maestro/`](../.maestro):

| File | Covers |
|------|--------|
| `flows/login.yaml` | Any credentials → Home dashboard |
| `flows/browse-production.yaml` | Home → Productions tab → production details → back |
| `flows/create-task.yaml` | Create a task (offline upload queue) → appears in list |
| `flows/sign-out.yaml` | Sign out → sign-in screen; re-login shows pristine seed |
| `subflows/login.yaml` | Reusable launch-fresh-and-sign-in |

## Prerequisites

- Maestro CLI: `brew install maestro` (or `curl -fsSL https://get.maestro.mobile.dev | bash`); verify `maestro --version`.
- A running Android emulator or connected device.
- Compose `testTag`s are exposed as Android `resource-id` via `testTagsAsResourceId`
  in `MainActivity` — that is what `id:` selectors match. Text selectors match visible strings.

## Android

```bash
./gradlew -q :androidApp:installDemoDebug   # task name contains "Demo" -> BuildKonfig demo flavor
maestro test .maestro/flows
```

Never mix demo and prod variant tasks in one Gradle invocation (`:shared` compiles a single
BuildKonfig flavor; the `androidApp` build script enforces this).

Debug selectors interactively: `maestro studio`.

CI (JUnit report):

```bash
maestro test --format junit --output build/maestro-report.xml .maestro/flows
```

## iOS (pending demo scheme)

No demo Xcode scheme exists yet. Manual path that works today:

```bash
./gradlew -q :composeApp:linkDebugFrameworkIosSimulatorArm64 -Pbuildkonfig.flavor=demo
# then build & install the app to a booted simulator via Xcode (do not clean), then:
maestro test .maestro/flows
```

Requires the gitignored `iosApp/iosApp/GoogleService-Info.plist` (`FirebaseApp.configure()`
runs at boot even in demo). On iOS, Maestro matches accessibility identifiers/labels — if a
`testTag` does not surface, fall back to the text anchors (seed titles "Midnight Harvest",
"Neon Tide") which work on both platforms.
