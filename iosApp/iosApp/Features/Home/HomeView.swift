import SwiftUI
import FrameZero

/// Home: native toolbar (Liquid Glass on iOS 26), swipeable tabs, floating tab switcher. Mirrors
/// composeApp/features/home `HomeContent` (pager + `FloatingBottomNav`) and replaces `HomeComponent`
/// on iOS.
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
        let colors = ColorTokens.forScheme(colorScheme)
        let background = Color(argb: colors.background)
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
        .overlay(alignment: .bottom) {
            // An overlay, not a safe-area inset: tab content scrolls underneath it (each tab reserves
            // `floatingBottomNavClearance`), as on Android.
            FloatingBottomNav(selection: $selectedTab)
                .padding(floatingBottomNavOuterPadding)
                .preGlassBackground {
                    // Fades scrolled content out behind the opaque nav; glass separates itself.
                    LinearGradient(colors: [background.opacity(0), background], startPoint: .top, endPoint: .center)
                        .padding(.top, -CGFloat(DesignTokens.shared.spacing.space24))
                        .ignoresSafeArea(edges: .bottom)
                }
        }
        .background(background)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Text("app_name")
                    .textStyle(DesignTokens.shared.typography.titleLarge)
                    .foregroundStyle(Color(argb: colors.accent))
                    .fixedSize()
                    .accessibilityAddTraits(.isHeader)
            }
            .withoutGlassBackground()
            ToolbarItem(placement: .topBarTrailing) {
                // No notifications screen yet — a no-op on Android too.
                Button {} label: { Image(systemName: "bell") }
                    .tint(Color(argb: colors.textPrimary))
                    .accessibilityLabel(Text("toolbar_notifications_cd"))
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button { navigate(.account) } label: { Image(systemName: "person") }
                    .glassButtonStyle(prominent: true)
                    .tint(Color(argb: colors.accent))
                    // Maestro's sign-out flow taps this by its label ("Settings").
                    .accessibilityLabel(Text("toolbar_settings_cd"))
            }
        }
    }
}
