# Custom design system over raw Material3

**Why:** semantic tokens (`AppTheme.colorSystem.textPrimary`,
`spacingSystem.md`, `radiusSystem.card`) decouple UI from Material's
naming and let dark/light flip in one place. Forbidding raw `Color(0xFF…)`
or magic `dp` numbers in feature code keeps the visual language
consistent.

**Not:**
- **Use `MaterialTheme.colorScheme` directly** — couples every screen to
  Material's vocabulary; a redesign means touching every file.
- **No theme system, hardcode colors** — guaranteed drift, no dark mode.
- **Pull a third-party design system** — over-prescriptive for a solo
  product.

**Cost:** new tokens land in `shared/design-tokens` (`DesignTokens`, raw values +
light/dark assembly, no Compose) and the matching `ColorSystem`/… field + `ThemeOptions`
mapping first, used second. The raw values live outside Compose so SwiftUI reads the
same ones through the iOS framework — one source of truth instead of a drifting Swift copy.
Documented as a rule in `CLAUDE.md`.
