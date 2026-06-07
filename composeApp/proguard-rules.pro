# R8 keep rules for the release variant.
#
# Intentionally minimal. R8 full mode is on by default (AGP 8.0+), and every
# dependency we use ships its own consumer ProGuard rules that R8 picks up
# automatically:
#   - Jetpack Compose (runtime/ui/foundation/material3) — adding a Compose-wide
#     keep here would defeat lambda grouping, sourceInformation() stripping,
#     composable-arg constant folding, and ComposerImpl devirtualization.
#   - kotlinx.serialization — serializers are generated at compile time by the
#     plugin applied in :shared; the library ships consumer rules.
#   - Ktor, Koin, Decompose, AndroidX Room/Paging — all ship consumer rules.
#
# Do NOT add speculative keeps. If a release build crashes with
# ClassNotFoundException / NoSuchMethodError, let R8 generate
# build/outputs/mapping/release/missing_rules.txt and add ONLY the narrow,
# specific rules it reports here.
