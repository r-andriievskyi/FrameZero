import SwiftUI
import FrameZero

typealias CreateTaskModel = ScreenModel<CreateTaskState, any CreateTaskIntent, any CreateTaskEvent>

extension CreateTaskModel {
    convenience init(graph: IosAppGraph, productionId: String, productionTitle: String) {
        let viewModel = graph.createTaskViewModelFactory.create(productionId: productionId, productionTitle: productionTitle)
        self.init(viewModel, state: viewModel.state, events: viewModel.events, onIntent: CreateTaskViewModel.onIntent)
    }
}

/// Mirrors composeApp/features/task-create `CreateTaskScreen`; accessibility identifiers = its
/// `CreateTaskTestTags`. Both events pop the screen, as `RootComponent` does: `Created` once the task
/// exists, `UploadEnqueued` once a task with an attachment is handed to the background uploader
/// (`BackgroundUrlSessionTaskUploadScheduler`, which finishes it even if the app is killed).
struct CreateTaskView: View {
    let model: CreateTaskModel
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        ScrollView {
            VStack(alignment: .leading, spacing: CGFloat(spacing.space24)) {
                FormSection("label_title") {
                    InputField(
                        placeholder: "title_placeholder",
                        text: binding(\.title) { CreateTaskIntentTitleChanged(title: $0) },
                        accessibilityID: "create-task:title"
                    )
                    if let error = state.titleError {
                        ErrorText(error.localized)
                            .accessibilityIdentifier("create-task:title-error")
                    }
                }
                FormSection("label_description") {
                    InputField(
                        placeholder: "description_placeholder",
                        text: binding(\.taskDescription) { CreateTaskIntentDescriptionChanged(taskDescription: $0) },
                        isMultiline: true
                    )
                }
                FormSection("label_attachment") {
                    AttachmentRow(
                        attachmentName: state.attachment?.name,
                        onAttach: { model.onIntent(CreateTaskIntentAttachFileClicked.shared) },
                        onRemove: { model.onIntent(CreateTaskIntentAttachmentRemoved.shared) }
                    )
                    if let error = state.attachmentError {
                        ErrorText(error.localized)
                    }
                }
                FormSection("label_assignee") {
                    AssigneeField(assignee: state.selectedAssignee) {
                        model.onIntent(CreateTaskIntentAssigneePickerOpened.shared)
                    }
                    Hint(
                        state.assignableMembers.isEmpty
                            ? String(localized: "assignee_subtitle_none")
                            : String(format: String(localized: "assignee_subtitle_count"), state.assignableMembers.count)
                    )
                }
                FormSection("label_participants") {
                    FlowLayout(spacing: CGFloat(spacing.space8)) {
                        ForEach(state.selectedParticipants, id: \.userId) { MemberChip(member: $0) }
                        AddParticipantsButton { model.onIntent(CreateTaskIntentParticipantPickerOpened.shared) }
                    }
                    Hint(
                        state.participantUserIds.isEmpty
                            ? String(localized: "participants_subtitle_none")
                            : String(format: String(localized: "participants_subtitle_count"), state.participantUserIds.count)
                    )
                }
                FormSection("label_priority") {
                    PrioritySelector(selected: state.priority) {
                        model.onIntent(CreateTaskIntentPriorityChanged(priority: $0))
                    }
                }
                FormSection("label_due_date") {
                    DueDateSection(
                        dueDate: state.dueDate,
                        onQuickSelect: { model.onIntent(CreateTaskIntentQuickDueDateSelected(option: $0)) },
                        onDateChange: { model.onIntent(CreateTaskIntentDueDateChanged(date: $0)) }
                    )
                }
            }
            .padding(CGFloat(spacing.space16))
        }
        .scrollDismissesKeyboard(.interactively)
        .safeAreaInset(edge: .bottom, spacing: 0) {
            VStack(spacing: 0) {
                Divider().overlay(Color(argb: colors.border))
                CtaButton(title: "create_task", isLoading: state.isLoading) {
                    model.onIntent(CreateTaskIntentSubmit.shared)
                }
                .accessibilityIdentifier("create-task:submit")
                .padding(CGFloat(spacing.space16))
            }
            .background(Color(argb: colors.background))
        }
        .background(Color(argb: colors.background))
        .navigationTitle("new_task_title")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("cancel") { dismiss() }
                    .tint(Color(argb: colors.textSecondary))
            }
        }
        .errorToast(state.errorToast?.localized) { model.onIntent(CreateTaskIntentToastDismissed.shared) }
        .gatedSheet(
            isPresented: state.isAssigneePickerVisible,
            onDismiss: { model.onIntent(CreateTaskIntentAssigneePickerDismissed.shared) }
        ) {
            MemberPickerSheet(
                searchPlaceholder: "assignee_search_placeholder",
                noResults: "assignee_no_results",
                query: binding(\.assigneeQuery) { CreateTaskIntentAssigneeSearchChanged(query: $0) },
                members: model.state.filteredAssignableMembers,
                noneOption: "assignee_unassign"
            ) { model.onIntent(CreateTaskIntentAssigneeSelected(userId: $0)) }
        }
        .gatedSheet(
            isPresented: state.isParticipantPickerVisible,
            onDismiss: { model.onIntent(CreateTaskIntentParticipantPickerDismissed.shared) }
        ) {
            MemberPickerSheet(
                searchPlaceholder: "participants_search_placeholder",
                noResults: "participants_no_results",
                query: binding(\.participantQuery) { CreateTaskIntentParticipantSearchChanged(query: $0) },
                members: model.state.filteredParticipantMembers,
                isSelected: { model.state.participantUserIds.contains($0.userId) }
            ) { userId in
                if let userId { model.onIntent(CreateTaskIntentParticipantToggled(userId: userId)) }
            }
        }
        .task { await model.observe() }
        .task { await model.handleEvents { _ in dismiss() } }
    }

    /// A text binding that reads `state` and writes through an intent.
    private func binding(
        _ keyPath: KeyPath<CreateTaskState, String>,
        _ intent: @escaping (String) -> any CreateTaskIntent
    ) -> Binding<String> {
        Binding(get: { model.state[keyPath: keyPath] }, set: { model.onIntent(intent($0)) })
    }
}

/// Caption heading over one form field; mirrors task-create's `SectionLabel` + spacing.
private struct FormSection<Content: View>: View {
    let title: LocalizedStringKey
    @ViewBuilder let content: Content
    @Environment(\.colorScheme) private var colorScheme

    init(_ title: LocalizedStringKey, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Text(title)
                .textStyle(DesignTokens.shared.typography.caption)
                .fontWeight(.bold)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textMuted))
                .accessibilityAddTraits(.isHeader)
            content
        }
    }
}

/// Muted helper line under a field.
private struct Hint: View {
    let text: String
    @Environment(\.colorScheme) private var colorScheme

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text)
            .textStyle(DesignTokens.shared.typography.bodySmall)
            .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textMuted))
    }
}
