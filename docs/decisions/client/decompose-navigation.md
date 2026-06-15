# Decompose for navigation & components

**Why:** Compose's own `NavHost` is Android-only; we need navigation that
works on iOS too. Decompose gives a `StackNavigation` plus a `Component`
lifecycle that survives configuration changes and is testable without a
Composable.

**Not:**
- **`androidx.navigation.compose`** — Android-only.
- **Voyager** — KMP, simpler, but lighter on lifecycle and DI seams.
- **Hand-rolled stack** — solving a solved problem.

**Cost:** extra concept (Component vs ViewModel vs Composable). All
stateful logic lives in `shared/features/<name>`; UI in `composeApp/features`
just renders it.
