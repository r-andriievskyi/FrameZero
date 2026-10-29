import SwiftUI
import FrameZero

typealias TasksListModel = PagedListModel<TaskListItemUi>

extension TasksListModel {
    /// A nil production id lists every task assigned to the user (the only entry point today —
    /// `RootComponent` pushes `TasksList` with `productionId = null` too).
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.tasksListViewModelFactory.create(productionId: nil)
        self.init(viewModel, items: viewModel.tasks)
    }
}

/// The task list behind the dashboard's "open tasks" card. Android's `TasksListScreen` is still a
/// stub, so this follows the plan's states (loading / error / empty / paged list) and the Productions
/// tab's paging behaviour; accessibility identifiers = `TasksListTestTags`.
struct TasksListView: View {
    let model: TasksListModel
    let onTaskTap: (String) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        Group {
            if state.isInitialLoad {
                FullScreenProgress()
                    .accessibilityIdentifier("tasks_list:loading")
            } else if let error = state.initialError {
                FullScreenError(message: error.localized) { model.refresh() }
                    .accessibilityIdentifier("tasks_list:error")
            } else if state.isEmpty {
                FullScreenMessage(text: "task_list_empty")
                    .accessibilityIdentifier("tasks_list:empty")
            } else {
                TasksListContent(model: model, onTaskTap: onTaskTap)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: ColorTokens.forScheme(colorScheme).background))
        .navigationTitle("task_list_title")
        .navigationBarTitleDisplayMode(.inline)
        .task { await model.observe() }
    }
}

private struct TasksListContent: View {
    let model: TasksListModel
    let onTaskTap: (String) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let spacing = DesignTokens.shared.spacing
        ScrollView {
            LazyVStack(spacing: CGFloat(spacing.space8)) {
                if state.isRefreshing {
                    ProgressView()
                        .tint(Color(argb: ColorTokens.forScheme(colorScheme).accent))
                }
                ForEach(Array(state.items.enumerated()), id: \.element.id) { index, task in
                    TaskListRow(task: task) { onTaskTap(task.id) }
                        .onAppear { model.onItemAppear(at: index) }
                }
                switch onEnum(of: state.append) {
                case .loading:
                    ProgressView()
                        .tint(Color(argb: ColorTokens.forScheme(colorScheme).accent))
                        .padding(CGFloat(spacing.space16))
                case .failed(let failure):
                    AppendErrorRow(message: failure.message.localized) { model.retry() }
                case .idle:
                    EmptyView()
                }
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.vertical, CGFloat(spacing.space8))
        }
        // Paging owns the reload; the inline spinner above shows it, so the gesture returns at once.
        .refreshable { model.refresh() }
        .accessibilityIdentifier("tasks_list:list")
    }
}

/// Footer shown when loading the next page fails; mirrors `AppendErrorRetry` in design_system's
/// `PagingLazyColumn`.
private struct AppendErrorRow: View {
    let message: String
    let onRetry: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Text(message)
                .textStyle(DesignTokens.shared.typography.bodySmall)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textSecondary))
                .multilineTextAlignment(.center)
            CtaButton(title: "action_retry", action: onRetry)
        }
        .padding(CGFloat(DesignTokens.shared.spacing.space16))
    }
}

/// Centered secondary text for an empty list.
private struct FullScreenMessage: View {
    let text: LocalizedStringKey
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Text(text)
            .textStyle(DesignTokens.shared.typography.bodyLarge)
            .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textSecondary))
            .multilineTextAlignment(.center)
            .padding(CGFloat(DesignTokens.shared.spacing.space16))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
