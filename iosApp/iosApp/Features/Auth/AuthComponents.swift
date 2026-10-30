import SwiftUI
import FrameZero

// Pieces shared by sign-in and register; mirror composeApp/features/auth `ui/components` plus the
// per-screen header/footer, which differ only in copy.

/// App name above both auth screens (Compose's `AuthLogoHeader`, minus the logo — no iOS asset yet).
struct AuthLogoHeader: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Text("app_name")
            .textStyle(DesignTokens.shared.typography.displayLarge)
            .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textPrimary))
    }
}

struct AuthHeader: View {
    let title: LocalizedStringKey
    let subtitle: LocalizedStringKey
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let typography = DesignTokens.shared.typography
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Text(title)
                .textStyle(typography.displayMedium)
                .foregroundStyle(Color(argb: colors.textPrimary))
            Text(subtitle)
                .textStyle(typography.bodyLarge)
                .foregroundStyle(Color(argb: colors.textSecondary))
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
    }
}

/// Caption above a field.
struct LabeledField<Content: View>: View {
    let label: LocalizedStringKey
    @ViewBuilder let content: Content
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Text(label)
                .textStyle(DesignTokens.shared.typography.labelSmall)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textPrimary))
            content
        }
    }
}

/// "New to FrameZero? Create account" / "Already have an account? Sign in".
struct AuthFooter: View {
    let prompt: LocalizedStringKey
    let action: LocalizedStringKey
    let isEnabled: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let typography = DesignTokens.shared.typography
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
            Text(prompt)
                .textStyle(typography.bodyMedium)
                .foregroundStyle(Color(argb: colors.textSecondary))
            Button(action: onTap) {
                Text(action)
                    .textStyle(typography.bodyMedium)
                    .foregroundStyle(Color(argb: colors.accent))
            }
            .buttonStyle(.plain)
            .disabled(!isEnabled)
        }
        .frame(maxWidth: .infinity)
    }
}
