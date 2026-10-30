import Observation
import FrameZero

/// Swift owner of one shared ViewModel (the iOS counterpart of a Decompose component's
/// `instanceKeeper`): mirrors its `state`, forwards intents, relays its one-shot `events` (`Never` for
/// ViewModels without them), and calls `onDestroy()` when released.
///
/// Create it once per route through `RouteModels`, never as a view's `@State` default — shared
/// ViewModels start work in `init`.
@MainActor
@Observable
final class ScreenModel<State: AnyObject, Intent, Event> {
    private(set) var state: State
    @ObservationIgnored private let viewModel: Instance_keeperInstanceKeeperInstance
    @ObservationIgnored private let stateFlow: SkieSwiftStateFlow<State>
    @ObservationIgnored private let send: (Intent) -> Void
    @ObservationIgnored private let events: SkieSwiftSharedFlow<Event>?

    init<ViewModel: Instance_keeperInstanceKeeperInstance>(
        _ viewModel: ViewModel,
        state: SkieSwiftStateFlow<State>,
        events: SkieSwiftSharedFlow<Event>,
        onIntent: @escaping (ViewModel) -> (Intent) -> Void
    ) {
        self.viewModel = viewModel
        self.stateFlow = state
        self.state = state.value
        self.events = events
        self.send = onIntent(viewModel)
    }

    init<ViewModel: Instance_keeperInstanceKeeperInstance>(
        _ viewModel: ViewModel,
        state: SkieSwiftStateFlow<State>,
        onIntent: @escaping (ViewModel) -> (Intent) -> Void
    ) where Event == Never {
        self.viewModel = viewModel
        self.stateFlow = state
        self.state = state.value
        self.events = nil
        self.send = onIntent(viewModel)
    }

    /// Mirrors `state` while the screen is visible (`.task` on the screen).
    func observe() async {
        for await state in stateFlow {
            self.state = state
        }
    }

    /// Hands each one-shot event to `handle` while the screen is visible (a second `.task`). Start it
    /// with the screen: the ViewModels' `SharedFlow`s drop events nobody is collecting.
    func handleEvents(_ handle: (Event) -> Void) async {
        guard let events else { return }
        for await event in events {
            handle(event)
        }
    }

    func onIntent(_ intent: Intent) {
        send(intent)
        // ViewModels reduce on `Dispatchers.Main.immediate`, so the new state is already in the
        // flow. Reading it now keeps text-field bindings in step with typing instead of waiting
        // for the async collector.
        state = stateFlow.value
    }

    deinit {
        viewModel.onDestroy()
    }
}
