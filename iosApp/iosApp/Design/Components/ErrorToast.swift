import SwiftUI
import FrameZero

private let autoDismiss: Duration = .seconds(4)
private let iconSize: CGFloat = 20
private let borderOpacity = 0.22

extension View {
    /// Screen-level error toast; mirrors Compose's `ToastHost` (design_system). A non-nil `message`
    /// slides in from the bottom, is announced to VoiceOver, and `onDismiss` fires after 4 s.
    func errorToast(_ message: String?, onDismiss: @escaping () -> Void) -> some View {
        overlay(alignment: .bottom) {
            ToastHost(message: message, onDismiss: onDismiss)
        }
    }
}

private struct ToastHost: View {
    let message: String?
    let onDismiss: () -> Void

    var body: some View {
        ZStack {
            if let message {
                ErrorToast(title: message)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .padding(CGFloat(DesignTokens.shared.spacing.space16))
        .animation(.default, value: message)
        .task(id: message) {
            guard let message else { return }
            AccessibilityNotification.Announcement(message).post()
            do {
                try await Task.sleep(for: autoDismiss)
                onDismiss()
            } catch {
                // Cancelled: the message changed or the screen went away.
            }
        }
    }
}

/// Error-severity toast card; mirrors Compose's `AppToast(severity = Error)`.
struct ErrorToast: View {
    let title: String
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let text = Color(argb: colors.errorText)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius14))
        HStack(spacing: CGFloat(spacing.space12)) {
            Image(systemName: "xmark.circle")
                .resizable()
                .frame(width: iconSize, height: iconSize)
                .accessibilityHidden(true)
            Text(title)
                .textStyle(DesignTokens.shared.typography.titleMedium)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .foregroundStyle(text)
        .padding(.horizontal, CGFloat(spacing.space16))
        .padding(.vertical, CGFloat(spacing.space12))
        .background(Color(argb: colors.errorSurface), in: shape)
        .overlay(shape.stroke(text.opacity(borderOpacity), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
    }
}

#Preview {
    ErrorToast(title: "Network error: couldn't reach the server")
        .padding()
}
