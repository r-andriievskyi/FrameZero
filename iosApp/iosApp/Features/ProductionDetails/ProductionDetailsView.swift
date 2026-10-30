import SwiftUI
import FrameZero

typealias ProductionDetailsModel = ScreenModel<ProductionDetailsState, any ProductionDetailsIntent, any ProductionDetailsEvent>

extension ProductionDetailsModel {
    convenience init(graph: IosAppGraph, productionId: String) {
        let viewModel = graph.productionDetailsViewModelFactory.create(productionId: productionId)
        self.init(viewModel, state: viewModel.state, events: viewModel.events, onIntent: ProductionDetailsViewModel.onIntent)
    }
}

private let deletingOverlayOpacity = 0.6

/// Mirrors composeApp/features/production-details `ProductionDetailsScreen`; accessibility identifiers =
/// its `ProductionDetailsTestTags`. `Deleted` pops the route, as `RootComponent` does.
struct ProductionDetailsView: View {
    let model: ProductionDetailsModel
    let onAddTask: (_ productionId: String, _ productionTitle: String) -> Void
    /// `ProductionDetailsComponent` refreshes tasks on every resume but the first (the ViewModel loads
    /// them on creation); coming back from create task is what makes a new task show up.
    @State private var hasAppeared = false
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        VStack(spacing: 0) {
            if let upload = state.pendingUpload {
                PendingUploadBanner(
                    upload: upload,
                    onRetry: { model.onIntent(ProductionDetailsIntentRetryUploadRequested.shared) },
                    onDismiss: { model.onIntent(ProductionDetailsIntentDismissUploadRequested.shared) }
                )
                .accessibilityIdentifier("production-details:pending-upload")
                .padding(.horizontal, CGFloat(DesignTokens.shared.spacing.space16))
                .padding(.vertical, CGFloat(DesignTokens.shared.spacing.space8))
            }
            if let detail = state.detail {
                DetailBody(detail: detail, tasks: state.tasks) {
                    model.onIntent(ProductionDetailsIntentAddTaskRequested.shared)
                }
                .accessibilityIdentifier("production-details:content")
            } else if let error = state.error {
                FullScreenError(message: error.localized) {
                    model.onIntent(ProductionDetailsIntentRefresh.shared)
                }
                .accessibilityIdentifier("production-details:error")
            } else if state.isLoading {
                FullScreenProgress()
                    .accessibilityIdentifier("production-details:loading")
            } else {
                Spacer()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: colors.background))
        .overlay {
            if state.isDeleting {
                FullScreenProgress()
                    .background(Color(argb: colors.background).opacity(deletingOverlayOpacity))
            }
        }
        .navigationTitle(state.detail?.title ?? "")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Menu {
                    Button("delete_production_menu", role: .destructive) {
                        model.onIntent(ProductionDetailsIntentDeleteRequested.shared)
                    }
                } label: {
                    Image(systemName: "ellipsis")
                }
                .accessibilityLabel(Text("cd_more_options"))
            }
        }
        // Cancel, swipe-away and Delete all end in `DeleteDismissed`; after Delete it's a no-op, as
        // `DeleteConfirmed` has already hidden the dialog.
        .gatedAlert(
            "delete_production_title",
            isPresented: state.isDeleteDialogVisible,
            onDismiss: { model.onIntent(ProductionDetailsIntentDeleteDismissed.shared) }
        ) {
            Button("cancel_action", role: .cancel) {}
            Button("delete_action", role: .destructive) {
                model.onIntent(ProductionDetailsIntentDeleteConfirmed.shared)
            }
        } message: {
            if let title = model.state.detail?.title, !title.trimmingCharacters(in: .whitespaces).isEmpty {
                Text(String(format: String(localized: "delete_production_body_named"), title))
            } else {
                Text("delete_production_body_unnamed")
            }
        }
        .gatedAlert(
            "couldnt_delete",
            isPresented: state.deleteError != nil,
            onDismiss: { model.onIntent(ProductionDetailsIntentDeleteErrorDismissed.shared) }
        ) {
            Button("ok_action") {}
        } message: {
            Text(model.state.deleteError?.localized ?? "")
        }
        .onAppear {
            if hasAppeared {
                model.onIntent(ProductionDetailsIntentRefreshTasks.shared)
            }
            hasAppeared = true
        }
        .task { await model.observe() }
        .task {
            await model.handleEvents { event in
                switch onEnum(of: event) {
                case .deleted: dismiss()
                case .addTaskRequested(let request): onAddTask(request.productionId, request.productionTitle)
                }
            }
        }
    }
}

private struct DetailBody: View {
    let detail: ProductionDetailUi
    let tasks: [ProductionTaskUi]
    let onAddTask: () -> Void

    var body: some View {
        let spacing = DesignTokens.shared.spacing
        ScrollView {
            VStack(spacing: CGFloat(spacing.space16)) {
                LoglineCard(detail: detail)
                PipelineCard(pipeline: detail.pipeline, currentPhase: detail.phase)
                DateCards(startDate: detail.startDateLabel, wrapDate: detail.wrapDateLabel)
                if let viewerCrew = detail.viewerCrew {
                    TeamCard(viewerCrew: viewerCrew)
                }
                TasksCard(tasks: tasks, onAddTask: onAddTask)
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.bottom, CGFloat(spacing.space24))
        }
    }
}
