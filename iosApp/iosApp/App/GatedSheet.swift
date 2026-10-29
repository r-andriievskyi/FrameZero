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
        modifier(GatedSheet(isPresented: isPresented, onDismiss: onDismiss, sheetContent: content))
    }
}

private struct GatedSheet<SheetContent: View>: ViewModifier {
    let isPresented: Bool
    let onDismiss: () -> Void
    let sheetContent: () -> SheetContent
    @Environment(\.isAppGated) private var isAppGated
    @State private var isShowing = false

    private var wantsSheet: Bool {
        isPresented && !isAppGated
    }

    func body(content: Content) -> some View {
        // Dismissals are classified in `onChange`, which sees the current values. A `Binding` setter
        // can't be trusted for that: SwiftUI may call one captured before the gate went up.
        content
            .sheet(isPresented: $isShowing, content: sheetContent)
            .onChange(of: wantsSheet, initial: true) { _, wanted in
                isShowing = wanted
            }
            .onChange(of: isShowing) { _, showing in
                // Closed while still wanted = the user dismissed it; the gate or the ViewModel
                // closing it has already turned `wantsSheet` off.
                if !showing && wantsSheet {
                    onDismiss()
                }
            }
    }
}
