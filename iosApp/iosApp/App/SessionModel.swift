import Observation
import FrameZero

/// Which top-level flow the app shows. Mirrors the root swap in Android's `RootComponent`
/// (composeApp/commonMain) — keep both in sync.
enum AppRoot: Equatable {
    case splash
    case auth
    case home
}

/// Owns the session-driven root. `Loading` never changes the root once one is chosen, so a
/// session re-resolving does not tear down the signed-in stack (same rule as `RootComponent`).
@MainActor
@Observable
final class SessionModel {
    let graph: IosAppGraph
    private(set) var root: AppRoot = .splash

    init(graph: IosAppGraph) {
        self.graph = graph
    }

    /// Runs for the app's lifetime (`.task` on the root view).
    func run() async {
        // Metro has no eager bindings: resolve the synchronizer so it starts observing the
        // session at launch rather than whenever something first asks for it.
        _ = graph.deviceTokenSynchronizer
        let sessionManager = graph.sessionManager
        let initialize = Task {
            // SessionManager.initialize() resolves failures into SessionState itself; a throw here
            // is only coroutine cancellation.
            try? await sessionManager.initialize()
        }
        defer { initialize.cancel() }

        for await state in sessionManager.state {
            switch onEnum(of: state) {
            case .loading:
                break
            case .loggedOut:
                root = .auth
            case .loggedIn:
                root = .home
            }
        }
    }
}
