import SwiftUI

extension EnvironmentValues {
    /// Whether the biometric lock or a force-update cover is up (set by `RootView`).
    @Entry var isAppGated = false
}

extension View {
    /// A `.sheet` driven by ViewModel state that steps aside while the app is gated. The gates are
    /// overlays in `RootView`, and a presented sheet would sit above them — so the sheet is dismissed
    /// while gated (without telling the ViewModel) and comes back once the gate lifts.
    func gatedSheet<SheetContent: View>(
        isPresented: Bool,
        onDismiss: @escaping () -> Void,
        @ViewBuilder content: @escaping () -> SheetContent
    ) -> some View {
        modifier(GatedPresentation(isPresented: isPresented, onDismiss: onDismiss) {
            SheetPresentation(isShowing: $0, sheetContent: content)
        })
    }

    /// An `.alert` driven by ViewModel state, gated like `gatedSheet`. `onDismiss` fires however the
    /// alert closes, button taps included (after the button's own action).
    func gatedAlert<Actions: View, Message: View>(
        _ title: LocalizedStringKey,
        isPresented: Bool,
        onDismiss: @escaping () -> Void,
        @ViewBuilder actions: @escaping () -> Actions,
        @ViewBuilder message: @escaping () -> Message
    ) -> some View {
        modifier(GatedPresentation(isPresented: isPresented, onDismiss: onDismiss) {
            AlertPresentation(title: title, isShowing: $0, actions: actions, message: message)
        })
    }
}

private struct GatedPresentation<Presentation: ViewModifier>: ViewModifier {
    let isPresented: Bool
    let onDismiss: () -> Void
    /// The presentation (sheet, alert), bound to whether it is showing.
    let presentation: (Binding<Bool>) -> Presentation
    @Environment(\.isAppGated) private var isAppGated
    @State private var isShowing = false

    private var wantsPresentation: Bool {
        isPresented && !isAppGated
    }

    func body(content: Content) -> some View {
        // Dismissals are classified in `onChange`, which sees the current values. A `Binding` setter
        // can't be trusted for that: SwiftUI may call one captured before the gate went up.
        content
            .modifier(presentation($isShowing))
            .onChange(of: wantsPresentation, initial: true) { _, wanted in
                isShowing = wanted
            }
            .onChange(of: isShowing) { _, showing in
                // Closed while still wanted = the user dismissed it; the gate or the ViewModel
                // closing it has already turned `wantsPresentation` off.
                if !showing && wantsPresentation {
                    onDismiss()
                }
            }
    }
}

private struct SheetPresentation<SheetContent: View>: ViewModifier {
    @Binding var isShowing: Bool
    let sheetContent: () -> SheetContent

    func body(content: Content) -> some View {
        content.sheet(isPresented: $isShowing, content: sheetContent)
    }
}

private struct AlertPresentation<Actions: View, Message: View>: ViewModifier {
    let title: LocalizedStringKey
    @Binding var isShowing: Bool
    let actions: () -> Actions
    let message: () -> Message

    func body(content: Content) -> some View {
        content.alert(title, isPresented: $isShowing, actions: actions, message: message)
    }
}
