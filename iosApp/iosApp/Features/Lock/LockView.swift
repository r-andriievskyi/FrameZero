import SwiftUI
import FrameZero

private let iconContainerSize: CGFloat = 88
private let iconSize: CGFloat = 40

/// Full-screen biometric lock; mirrors composeApp `BiometricLockOverlay`. Opaque and hit-testable
/// over the whole window, so nothing underneath (including swipe-back) can be reached.
struct LockView: View {
    let onUnlock: (BiometricPromptText) -> Void
    let onSignOut: () -> Void
    @State private var prompted = false
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        VStack(spacing: 0) {
            Image(systemName: "lock")
                .resizable()
                .scaledToFit()
                .frame(width: iconSize, height: iconSize)
                .foregroundStyle(Color(argb: colors.accent))
                .frame(width: iconContainerSize, height: iconContainerSize)
                .background(
                    Color(argb: colors.accentSurface),
                    in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
                )
                .accessibilityHidden(true)
                .padding(.bottom, CGFloat(spacing.space24))
            Text("lock_title")
                .textStyle(typography.titleMedium)
                .foregroundStyle(Color(argb: colors.textPrimary))
                .accessibilityAddTraits(.isHeader)
                .padding(.bottom, CGFloat(spacing.space8))
            Text("lock_subtitle")
                .textStyle(typography.bodyMedium)
                .foregroundStyle(Color(argb: colors.textMuted))
                .padding(.bottom, CGFloat(spacing.space32))
            CtaButton(title: "lock_unlock_button") { onUnlock(prompt) }
                .padding(.bottom, CGFloat(spacing.space16))
            TextActionButton(title: "lock_sign_out_button", action: onSignOut)
        }
        .multilineTextAlignment(.center)
        .padding(.horizontal, CGFloat(spacing.space32))
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: colors.background))
        .contentShape(Rectangle())
        // Present the prompt on its own once per return to the foreground — the lock engages while
        // backgrounded, and Face ID can't be presented from there. Not on every `.active`: the
        // prompt itself flips the scene to inactive and back, which would re-prompt after a cancel.
        .task(id: scenePhase) {
            switch scenePhase {
            case .background:
                prompted = false
            case .active where !prompted:
                prompted = true
                onUnlock(prompt)
            default:
                break
            }
        }
    }

    private var prompt: BiometricPromptText {
        BiometricPromptText(
            title: String(localized: "lock_prompt_title"),
            subtitle: String(localized: "lock_prompt_subtitle"),
            negativeButton: String(localized: "lock_prompt_cancel")
        )
    }
}

#Preview {
    LockView(onUnlock: { _ in }, onSignOut: {})
}
