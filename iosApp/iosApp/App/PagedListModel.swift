import Observation
import FrameZero

/// Swift owner of a paged list — `ScreenModel`'s counterpart for ViewModels that expose
/// `Flow<PagingData>` instead of state + intents. Presents the flow through the shared `PagedItems`
/// (the iOS stand-in for `collectAsLazyPagingItems`), mirrors its state, and on release closes it and
/// calls the ViewModel's `onDestroy()`.
///
/// Create it once per route through `RouteModels`, like `ScreenModel`.
@MainActor
@Observable
final class PagedListModel<Item: AnyObject> {
    private(set) var state: PagedListState<Item>
    @ObservationIgnored private let viewModel: Instance_keeperInstanceKeeperInstance
    @ObservationIgnored private let pagedItems: PagedItems<Item>

    init(_ viewModel: Instance_keeperInstanceKeeperInstance, items: SkieSwiftFlow<Paging_commonPagingData<Item>>) {
        self.viewModel = viewModel
        self.pagedItems = PagedItems(flow: items)
        self.state = pagedItems.state.value
    }

    /// Mirrors the paged state while the screen is visible (`.task` on the screen).
    func observe() async {
        for await state in pagedItems.state {
            self.state = state
        }
    }

    /// Call when the row at `index` appears: tells Paging how far the user scrolled, which loads
    /// the next page in time.
    func onItemAppear(at index: Int) {
        _ = pagedItems.get(index: Int32(index))
    }

    func refresh() {
        pagedItems.refresh()
    }

    func retry() {
        pagedItems.retry()
    }

    deinit {
        pagedItems.close()
        viewModel.onDestroy()
    }
}
