import Observation
import SwiftUI
import FrameZero

/// Which top-level flow the app shows. Mirrors the root swap in Android's `RootComponent`
/// (composeApp/commonMain) — keep both in sync.
enum AppRoot: Equatable {
    case splash
    case auth
    case home
}

/// Which force-update cover is up, if any (`ForceUpdateState` without its payload).
enum UpdateGate: Equatable {
    case none
    case soft
    case hard
}

/// The iOS counterpart of Android's `RootComponent` policy (keep both in sync): the session-driven
/// root, the biometric lock gate, the force-update gate and their lifecycle hooks. Deep links are
/// consumed by `SignedInView`, which exists exactly while signed in.
///
/// `Loading` never changes the root once one is chosen, so a session re-resolving does not tear
/// down the signed-in stack.
@MainActor
@Observable
final class RootModel {
    let graph: IosAppGraph
    private(set) var root: AppRoot = .splash
    private(set) var updateGate = UpdateGate.none
    private var lockState = AppLockState.unlocked
    @ObservationIgnored private var wasBackgrounded = false

    /// The lock covers the app only while signed in — a locked session at the auth screen would make
    /// no sense.
    var isLocked: Bool {
        root == .home && lockState == .locked
    }

    init(graph: IosAppGraph) {
        self.graph = graph
    }

    /// Runs for the app's lifetime (`.task` on the root view).
    func run() async {
        // Metro has no eager bindings: resolve the synchronizer so it starts observing the
        // session at launch rather than whenever something first asks for it.
        _ = graph.deviceTokenSynchronizer
        let sessionManager = graph.sessionManager
        let tasks: [Task<Void, Never>] = [
            Task {
                // SessionManager.initialize() resolves failures into SessionState itself; a throw
                // here is only coroutine cancellation.
                _ = try? await sessionManager.initialize()
            },
            Task { await refreshUpdateGate() },
            Task {
                for await state in graph.appLockController.lockState {
                    lockState = state
                }
            },
            Task {
                for await state in graph.forceUpdateController.state {
                    updateGate = switch onEnum(of: state) {
                    case .none: .none
                    case .soft: .soft
                    case .hard: .hard
                    }
                }
            },
        ]
        defer { tasks.forEach { $0.cancel() } }

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

    /// Re-locks on background; re-checks the update gate on every return to the foreground. The
    /// cold-start check is in `run()` — like `RootComponent`'s `doOnResume`, which skips the first
    /// resume.
    func scenePhaseChanged(to phase: ScenePhase) {
        switch phase {
        case .background:
            wasBackgrounded = true
            graph.appLockController.onBackgrounded()
        case .active where wasBackgrounded:
            wasBackgrounded = false
            Task { await refreshUpdateGate() }
        default:
            break
        }
    }

    func unlock(prompt: BiometricPromptText) {
        Task { _ = try? await graph.appLockController.authenticate(prompt: prompt) }
    }

    /// The lock's only escape.
    func signOut() {
        Task { _ = try? await graph.sessionManager.logout() }
    }

    func openStore() {
        graph.forceUpdateController.openStore()
    }

    func dismissSoftUpdate() {
        graph.forceUpdateController.dismissSoft()
    }

    private func refreshUpdateGate() async {
        _ = try? await graph.forceUpdateController.refresh()
    }
}
