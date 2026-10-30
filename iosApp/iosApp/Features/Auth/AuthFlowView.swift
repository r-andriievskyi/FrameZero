import SwiftUI
import FrameZero

/// Screens of the signed-out flow. Mirrors `AuthComponent.Config` (shared/features/auth).
enum AuthScreen: Hashable {
    case signIn
    case register
}

/// Signed-out flow: sign-in at the root, register pushed on top (Android: `bringToFront(Register)`,
/// back = pop). Leaving the auth root (sign-in succeeded) drops this view and with it every model.
struct AuthFlowView: View {
    let graph: IosAppGraph
    @State private var path: [AuthScreen] = []
    @State private var models = RouteModels<AuthScreen>()

    var body: some View {
        NavigationStack(path: $path) {
            screen(.signIn)
                .toolbar(.hidden, for: .navigationBar)
                .navigationDestination(for: AuthScreen.self, destination: screen)
        }
        .onChange(of: path) { _, path in
            models.retain([.signIn] + path)
        }
    }

    @ViewBuilder
    private func screen(_ screen: AuthScreen) -> some View {
        switch screen {
        case .signIn:
            SignInView(
                model: models.model(for: .signIn) { SignInModel(graph: graph) },
                onCreateAccount: {
                    if path.last != .register {
                        path.append(.register)
                    }
                }
            )
        case .register:
            RegisterView(
                model: models.model(for: .register) { RegisterModel(graph: graph) },
                onSignIn: { path.removeLast() }
            )
        }
    }
}
