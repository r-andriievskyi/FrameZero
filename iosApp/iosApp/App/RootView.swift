import SwiftUI

/// Root flow plus the full-screen gates drawn over it, in Android's `RootContent` order: lock, then
/// force update on top. Gates are overlays rather than `fullScreenCover`s so they can appear no
/// matter what the stack below is presenting, and they hide what they cover from VoiceOver.
struct RootView: View {
    let model: RootModel
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        let isGated = model.isLocked || model.updateGate != .none
        ZStack {
            Group {
                switch model.root {
                case .splash:
                    SplashView()
                case .auth:
                    AuthFlowView(graph: model.graph)
                case .home:
                    SignedInView(graph: model.graph)
                }
            }
            .accessibilityHidden(isGated)
            .environment(\.isAppGated, isGated)
            if model.isLocked {
                LockView(onUnlock: model.unlock, onSignOut: model.signOut)
                    .accessibilityHidden(model.updateGate != .none)
            }
            switch model.updateGate {
            case .none:
                EmptyView()
            case .soft:
                SoftUpdateView(onUpdate: model.openStore, onDismiss: model.dismissSoftUpdate)
            case .hard:
                HardUpdateView(onUpdate: model.openStore)
            }
        }
        .task { await model.run() }
        .onChange(of: scenePhase) { _, phase in
            model.scenePhaseChanged(to: phase)
        }
    }
}
