import SwiftUI
import FrameZero

/// Home: toolbar, swipeable tabs, floating tab switcher. Mirrors composeApp/features/home
/// `HomeContent` (pager + `FloatingBottomNav`) and replaces `HomeComponent` on iOS.
///
/// Tab models live as long as Home — like `HomeComponent`'s per-tab child contexts — so switching
/// tabs keeps their state.
struct HomeView: View {
    let graph: IosAppGraph
    let navigate: (Route) -> Void
    @State private var selectedTab = HomeTab.dashboard
    @State private var tabModels = RouteModels<HomeTab>()
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let background = Color(argb: ColorTokens.forScheme(colorScheme).background)
        VStack(spacing: 0) {
            HomeToolbar(
                onNotifications: {}, // No notifications screen yet — a no-op on Android too.
                onAccount: { navigate(.account) }
            )
            TabView(selection: $selectedTab) {
                DashboardTabView(
                    model: tabModels.model(for: .dashboard) { DashboardTabModel(graph: graph) },
                    onTaskTap: { navigate(.taskDetails(taskId: $0)) },
                    onTasksTap: { navigate(.tasksList) }
                )
                .safeAreaPadding(.bottom, floatingBottomNavClearance)
                .tag(HomeTab.dashboard)
                ProductionsTabView(
                    model: tabModels.model(for: .productions) { ProductionsTabModel(graph: graph) },
                    onProductionTap: { navigate(.productionDetails(productionId: $0)) },
                    onCreateProduction: { navigate(.createProduction) }
                )
                .safeAreaPadding(.bottom, floatingBottomNavClearance)
                .tag(HomeTab.productions)
                ScheduleTabView(
                    model: tabModels.model(for: .schedule) { ScheduleTabModel(graph: graph) },
                    onTaskTap: { navigate(.taskDetails(taskId: $0)) }
                )
                .safeAreaPadding(.bottom, floatingBottomNavClearance)
                .tag(HomeTab.schedule)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .overlay(alignment: .bottom) {
            // An overlay, not a safe-area inset: tab content scrolls underneath it (each tab reserves
            // `floatingBottomNavClearance`), as on Android.
            FloatingBottomNav(selection: $selectedTab)
                .padding(floatingBottomNavOuterPadding)
                .background {
                    // Fades scrolled content out behind the floating nav.
                    LinearGradient(colors: [background.opacity(0), background], startPoint: .top, endPoint: .center)
                        .padding(.top, -CGFloat(DesignTokens.shared.spacing.space24))
                        .ignoresSafeArea(edges: .bottom)
                }
        }
        .background(background)
        .toolbar(.hidden, for: .navigationBar)
    }
}
