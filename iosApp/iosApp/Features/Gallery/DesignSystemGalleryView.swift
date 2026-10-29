import SwiftUI
import FrameZero

// The developer-options catalog of design tokens and components; mirrors composeApp
// `DesignSystemGalleryScreen` and its sections. Side by side with Android it shows the shared
// `DesignTokens` render the same on both. Its copy is English-only developer text, hardcoded as on
// Android; labels pass through `String` so Xcode doesn't extract them into the String Catalog.

private let swatchSize: CGFloat = 44
private let radiusSwatchSize: CGFloat = 56
private let spacingMarkerSize: CGFloat = 20

struct DesignSystemGalleryView: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let spacing = DesignTokens.shared.spacing
        ScrollView {
            VStack(alignment: .leading, spacing: CGFloat(spacing.space24)) {
                ColorsSection()
                TypographySection()
                SpacingSection()
                RadiusSection()
                ButtonsSection()
                InputSection()
                ToastSection()
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.top, CGFloat(spacing.space16))
            .padding(.bottom, CGFloat(spacing.space32))
        }
        .background(Color(argb: ColorTokens.forScheme(colorScheme).background))
        .navigationTitle(Text(verbatim: "Design System"))
        .navigationBarTitleDisplayMode(.inline)
    }
}

/// Titled container shared by every section; mirrors `GallerySection`.
private struct GallerySection<Content: View>: View {
    let title: String
    @ViewBuilder let content: Content
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space12)) {
            Text(verbatim: title)
                .textStyle(DesignTokens.shared.typography.titleSmall)
                .fontWeight(.semibold)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textPrimary))
                .accessibilityAddTraits(.isHeader)
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// A token name beside its sample, the row shape of the colors, spacing and radius sections.
private struct TokenRow<Sample: View>: View {
    let name: String
    @ViewBuilder let sample: Sample
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
            sample
            Text(verbatim: name)
                .textStyle(DesignTokens.shared.typography.bodyMedium)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textPrimary))
        }
    }
}

private struct ColorsSection: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let swatches: [(String, Int64)] = [
            ("background", colors.background),
            ("surfaceElevated", colors.surfaceElevated),
            ("cardBackground", colors.cardBackground),
            ("accent", colors.accent),
            ("accentDim", colors.accentDim),
            ("textPrimary", colors.textPrimary),
            ("textSecondary", colors.textSecondary),
            ("textMuted", colors.textMuted),
            ("successText", colors.successText),
            ("warningText", colors.warningText),
            ("errorText", colors.errorText),
            ("border", colors.border),
        ]
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        GallerySection(title: "Colors") {
            VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
                ForEach(swatches, id: \.0) { name, argb in
                    TokenRow(name: name) {
                        shape
                            .fill(Color(argb: argb))
                            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
                            .frame(width: swatchSize, height: swatchSize)
                    }
                }
            }
        }
    }
}

private struct TypographySection: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let typography = DesignTokens.shared.typography
        let samples: [(String, TextStyleToken)] = [
            ("displayLarge", typography.displayLarge),
            ("titleLarge", typography.titleLarge),
            ("titleMedium", typography.titleMedium),
            ("titleSmall", typography.titleSmall),
            ("bodyLarge", typography.bodyLarge),
            ("bodyMedium", typography.bodyMedium),
            ("bodySmall", typography.bodySmall),
            ("labelLarge", typography.labelLarge),
            ("labelMedium", typography.labelMedium),
            ("caption", typography.caption),
            ("monoMedium", typography.monoMedium),
        ]
        GallerySection(title: "Typography") {
            VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
                ForEach(samples, id: \.0) { name, style in
                    Text(verbatim: name)
                        .textStyle(style)
                        .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textPrimary))
                }
            }
        }
    }
}

private struct SpacingSection: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let tokens: [(String, Float)] = [
            ("space2", spacing.space2),
            ("space4", spacing.space4),
            ("space8", spacing.space8),
            ("space12", spacing.space12),
            ("space16", spacing.space16),
            ("space24", spacing.space24),
            ("space32", spacing.space32),
        ]
        let marker = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4))
        GallerySection(title: "Spacing") {
            VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                ForEach(tokens, id: \.0) { name, token in
                    TokenRow(name: name) {
                        // The token is the gap between the two markers.
                        HStack(spacing: CGFloat(token)) {
                            marker.fill(Color(argb: colors.accent)).frame(width: spacingMarkerSize, height: spacingMarkerSize)
                            marker.fill(Color(argb: colors.accentDim)).frame(width: spacingMarkerSize, height: spacingMarkerSize)
                        }
                    }
                }
            }
        }
    }
}

private struct RadiusSection: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let radius = DesignTokens.shared.radius
        let tokens: [(String, Float)] = [
            ("radius4", radius.radius4),
            ("radius8", radius.radius8),
            ("radius14", radius.radius14),
            ("radius16", radius.radius16),
            ("radiusMax", radius.radiusMax),
        ]
        GallerySection(title: "Radius") {
            VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
                ForEach(tokens, id: \.0) { name, token in
                    TokenRow(name: name) {
                        let shape = RoundedRectangle(cornerRadius: CGFloat(token))
                        shape
                            .fill(Color(argb: colors.accentSurface))
                            .overlay(shape.stroke(Color(argb: colors.accent), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
                            .frame(width: radiusSwatchSize, height: radiusSwatchSize)
                    }
                }
            }
        }
    }
}

private struct ButtonsSection: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        GallerySection(title: "Buttons") {
            VStack(spacing: CGFloat(DesignTokens.shared.spacing.space12)) {
                CtaButton(title: verbatimKey("Primary CTA")) {}
                CtaButton(title: verbatimKey("Loading"), isLoading: true) {}
                OutlinedCtaButton(
                    title: verbatimKey("Outlined CTA"),
                    color: Color(argb: ColorTokens.forScheme(colorScheme).accent)
                ) {}
            }
        }
    }
}

private struct InputSection: View {
    @State private var value = ""

    var body: some View {
        GallerySection(title: "Input field") {
            InputField(placeholder: LocalizedStringResource(String.LocalizationValue(placeholder)), text: $value)
        }
    }

    private var placeholder: String { "Type something…" }
}

private struct ToastSection: View {
    var body: some View {
        GallerySection(title: "Toasts") {
            VStack(spacing: CGFloat(DesignTokens.shared.spacing.space12)) {
                AppToast(title: "Saved successfully", severity: .success)
                AppToast(title: "Check your connection", severity: .warning, description: "You appear to be offline.")
                AppToast(title: "Something went wrong", severity: .error, actionLabel: verbatimKey("Retry")) {}
            }
        }
    }
}

/// A key built from a `String` rather than a literal, so Xcode leaves it out of the String Catalog;
/// with no catalog entry it renders as written.
private func verbatimKey(_ text: String) -> LocalizedStringKey {
    LocalizedStringKey(text)
}

#Preview {
    NavigationStack { DesignSystemGalleryView() }
}
