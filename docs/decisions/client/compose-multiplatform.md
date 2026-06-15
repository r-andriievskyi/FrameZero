# Compose Multiplatform

**Why:** one UI toolkit across Android and iOS — same composables, same
state model, written once. (Desktop/Web are supported by the toolkit but we
ship neither — see [expect-actual](expect-actual.md).)

**Not:**
- **SwiftUI on iOS, Compose on Android** — duplicate every screen, drift
  on day one.
- **KMP shared logic + native UI per platform** — viable, but doubles
  the surface a solo dev maintains.
- **Flutter** — different language, different state model, no link to
  the Kotlin domain code.

**Cost:** iOS Compose is younger than Android Compose; some platform
controls (camera, maps) still need wrapping.
