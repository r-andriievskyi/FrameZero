# Feature module split: `shared/features/*` + `composeApp/features/*`

**Why:** every feature has two halves — pure-Kotlin logic (Component,
ViewModel, state, intents) in `shared/features/<name>` and Compose UI in
`composeApp/features/<name>`. The logic half has zero Compose dependency,
so it's reusable, testable headlessly, and survives a UI rewrite.

**Not:**
- **One module per feature with UI + logic mixed** — drags Compose into
  unit tests and couples logic to the UI toolkit.
- **One giant `:shared` + one giant `:composeApp`** — no isolation,
  every change rebuilds everything.

**Cost:** more modules, more `build.gradle.kts` files. Convention plugins
in `build-logic/` keep them boilerplate-free.
