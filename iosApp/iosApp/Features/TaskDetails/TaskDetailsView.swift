import SwiftUI
import FrameZero

typealias TaskDetailsModel = ScreenModel<TaskDetailsState, any TaskDetailsIntent, Never>

extension TaskDetailsModel {
    convenience init(graph: IosAppGraph, taskId: String) {
        let viewModel = graph.taskDetailsViewModelFactory.create(taskId: taskId)
        self.init(viewModel, state: viewModel.state, onIntent: TaskDetailsViewModel.onIntent)
    }
}

/// Mirrors composeApp/features/task-details `TaskDetailsScreen`; accessibility identifiers = its
/// `TaskDetailsTestTags`.
struct TaskDetailsView: View {
    let model: TaskDetailsModel
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        Group {
            if state.isLoading {
                FullScreenProgress()
                    .accessibilityIdentifier("task-details:loading")
            } else if let error = state.error {
                FullScreenError(message: error.localized) {
                    model.onIntent(TaskDetailsIntentRefresh.shared)
                }
                .accessibilityIdentifier("task-details:error")
            } else {
                TaskDetailsContent(model: model)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: ColorTokens.forScheme(colorScheme).background))
        .navigationTitle("task_details_title")
        .navigationBarTitleDisplayMode(.inline)
        // The two come from unrelated actions, so at most one is set in practice; participants wins
        // only to give the dismissal a stable target (as on Android).
        .errorToast((state.participantsError ?? state.markCompleteError)?.localized) {
            model.onIntent(
                model.state.participantsError != nil
                    ? TaskDetailsIntentParticipantsErrorDismissed.shared
                    : TaskDetailsIntentMarkCompleteErrorDismissed.shared
            )
        }
        .gatedSheet(
            isPresented: state.isParticipantPickerVisible,
            onDismiss: { model.onIntent(TaskDetailsIntentParticipantPickerDismissed.shared) }
        ) {
            // Each tap saves straight away; rows are disabled while a save runs.
            MemberPickerSheet(
                searchPlaceholder: "task_details_participants_search_placeholder",
                noResults: "task_details_participants_no_results",
                query: Binding(
                    get: { model.state.participantQuery },
                    set: { model.onIntent(TaskDetailsIntentParticipantSearchChanged(query: $0)) }
                ),
                members: model.state.filteredAssignableMembers,
                isSelected: { member in model.state.participants.contains { $0.userId == member.userId } },
                isDisabled: model.state.isUpdatingParticipants
            ) { userId in
                if let userId { model.onIntent(TaskDetailsIntentParticipantToggled(userId: userId)) }
            }
        }
        .task { await model.observe() }
    }
}

private struct TaskDetailsContent: View {
    let model: TaskDetailsModel
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                HStack {
                    Text(state.productionName)
                        .textStyle(typography.titleLarge)
                        .foregroundStyle(Color(argb: colors.textSecondary))
                    Spacer()
                    PriorityBadge(priority: state.priority)
                }
                .padding(.bottom, CGFloat(spacing.space8))
                Text(state.title)
                    .textStyle(typography.displayMedium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityAddTraits(.isHeader)
                    .padding(.bottom, CGFloat(spacing.space16))
                AssigneeDueRow(assignee: state.assignee, dueDate: state.dueDate, isDueToday: state.isDueToday)
                    .padding(.bottom, CGFloat(spacing.space24))
                ParticipantsSection(participants: state.participants) {
                    model.onIntent(TaskDetailsIntentParticipantPickerOpened.shared)
                }
                .padding(.bottom, CGFloat(spacing.space24))
                if !state.taskDescription.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    Text(state.taskDescription)
                        .textStyle(typography.bodyLarge)
                        .foregroundStyle(Color(argb: colors.textSecondary))
                }
                if let attachment = state.attachment {
                    AttachmentCard(
                        attachment: attachment,
                        isDownloading: state.isDownloadingAttachment,
                        errorMessage: state.attachmentError?.localized
                    ) {
                        model.onIntent(TaskDetailsIntentDownloadAttachment.shared)
                    }
                    .padding(.top, CGFloat(spacing.space24))
                }
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.vertical, CGFloat(spacing.space8))
        }
        .accessibilityIdentifier("task-details:content")
        .safeAreaInset(edge: .bottom) {
            if state.showMarkCompleteButton {
                CtaButton(title: "task_details_mark_complete") {
                    model.onIntent(TaskDetailsIntentMarkComplete.shared)
                }
                .accessibilityIdentifier("task-details:mark-complete")
                .padding(CGFloat(spacing.space16))
                .background(Color(argb: colors.background))
            }
        }
    }
}
