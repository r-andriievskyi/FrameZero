import SwiftUI
import FrameZero

// Full-screen load states; mirror Compose's `FullScreenProgress`, `FullScreenError` and
// `OfflineBanner` (design_system).

struct FullScreenProgress: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        ProgressView()
            .tint(Color(argb: ColorTokens.forScheme(colorScheme).accent))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Centered message with an optional retry button.
struct FullScreenError: View {
    var message: String = String(localized: "error_generic_message")
    var onRetry: (() -> Void)?
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
            Text(message)
                .textStyle(DesignTokens.shared.typography.bodyLarge)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textSecondary))
                .multilineTextAlignment(.center)
            if let onRetry {
                CtaButton(title: "action_retry", action: onRetry)
                    .containerRelativeFrame(.horizontal) { width, _ in width / 2 }
            }
        }
        .padding(CGFloat(DesignTokens.shared.spacing.space16))
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Offline notice for screens that reload on their own once connectivity returns (no retry button).
struct OfflineBanner: View {
    let message: String
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        Text(message)
            .textStyle(DesignTokens.shared.typography.bodyLarge)
            .foregroundStyle(Color(argb: colors.warningText))
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
            .padding(CGFloat(spacing.space16))
            .background(
                Color(argb: colors.warningSurface),
                in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius14))
            )
            .padding(CGFloat(spacing.space16))
            .frame(maxHeight: .infinity)
    }
}

#Preview {
    VStack {
        FullScreenError(onRetry: {})
        OfflineBanner(message: String(localized: "error_home_offline"))
    }
}
