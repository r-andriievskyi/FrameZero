import SwiftUI
import FrameZero

private let autoDismiss: Duration = .seconds(4)

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
                AppToast(title: message, severity: .error)
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
