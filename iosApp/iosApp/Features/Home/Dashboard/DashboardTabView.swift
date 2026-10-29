import SwiftUI
import FrameZero

typealias DashboardTabModel = ScreenModel<DashboardTabState, any DashboardTabIntent, Never>

extension DashboardTabModel {
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.dashboardTabViewModel
        self.init(viewModel, state: viewModel.state, onIntent: DashboardTabViewModel.onIntent)
    }
}

/// Mirrors composeApp/features/home `DashboardTab`; accessibility identifiers = its `DashboardTestTags`.
struct DashboardTabView: View {
    let model: DashboardTabModel
    let onTaskTap: (String) -> Void
    let onTasksTap: () -> Void

    var body: some View {
        let state = model.state
        Group {
            if let error = state.error {
                if error.autoRetries {
                    OfflineBanner(message: error.message.localized)
                } else {
                    FullScreenError(message: error.message.localized) {
                        model.onIntent(DashboardTabIntentRetry.shared)
                    }
                }
            } else if state.isLoading {
                FullScreenProgress()
            } else if let dashboard = state.dashboard {
                DashboardContent(dashboard: dashboard, onTaskTap: onTaskTap, onTasksTap: onTasksTap)
            }
        }
        .task { await model.observe() }
    }
}

private struct DashboardContent: View {
    let dashboard: DashboardUi
    let onTaskTap: (String) -> Void
    let onTasksTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text(String(format: String(localized: "greeting_hello"), dashboard.displayName))
                    .textStyle(typography.displayMedium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("dashboard:greeting")
                    .padding(.bottom, CGFloat(spacing.space16))
                HStack(spacing: CGFloat(spacing.space8)) {
                    // No destination yet — Android's card is a no-op too.
                    StatCard(
                        systemImage: "movieclapper",
                        value: dashboard.stats.activeProjects,
                        label: "stats_active_projects"
                    )
                    .accessibilityIdentifier("dashboard:stat:active-productions")
                    StatCard(
                        systemImage: "checklist",
                        value: dashboard.stats.openTasks,
                        label: "stats_open_tasks",
                        action: onTasksTap
                    )
                    .accessibilityIdentifier("dashboard:stat:open-tasks")
                }
                .fixedSize(horizontal: false, vertical: true) // equal-height cards
                if !dashboard.myTasks.isEmpty {
                    Text("my_tasks_title")
                        .textStyle(typography.titleMedium)
                        .foregroundStyle(Color(argb: colors.textPrimary))
                        .accessibilityAddTraits(.isHeader)
                        .accessibilityIdentifier("dashboard:my-tasks:section")
                        .padding(.top, CGFloat(spacing.space24))
                        .padding(.bottom, CGFloat(spacing.space8))
                    VStack(spacing: CGFloat(spacing.space8)) {
                        ForEach(dashboard.myTasks, id: \.id) { task in
                            MyTaskRow(task: task) { onTaskTap(task.id) }
                                .accessibilityIdentifier("dashboard:my-tasks:row:\(task.id)")
                        }
                    }
                }
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.vertical, CGFloat(spacing.space24))
        }
    }
}
