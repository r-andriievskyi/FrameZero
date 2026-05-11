# Kotlin Multiplatform

**Why:** one engineer, four targets (Android, iOS, Desktop, Web). KMP lets
domain models, repositories, ViewModels, and DTOs live in `commonMain` and
ship to all of them. Platform code is the thin shell, not the business.

**Not:**
- **Native per platform (Kotlin + Swift + TS)** — 4× the work, 4× the
  drift, 4× the bugs.
- **Flutter / React Native** — second runtime + second language; loses
  the typed `shared` contract with the Kotlin server.

**Cost:** iOS framework linking (`embedAndSignAppleFrameworkForXcode`)
is a moving target; Wasm is still maturing.
