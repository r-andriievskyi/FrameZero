import SwiftUI
import FrameZero

private let chipAvatarSize: CGFloat = 24
private let rowAvatarSize: CGFloat = 32

/// The task's participants as chips plus a "Manage" entry to the picker; mirrors
/// composeApp/features/task-details `ParticipantsSection`.
struct ParticipantsSection: View {
    let participants: [TaskDetailsMember]
    let onManage: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
            HStack {
                Text("task_details_participants")
                    .textStyle(typography.labelLarge)
                    .foregroundStyle(Color(argb: colors.textMuted))
                Spacer()
                Button(action: onManage) {
                    Text("task_details_participants_manage")
                        .textStyle(typography.labelLarge)
                        .foregroundStyle(Color(argb: colors.textSecondary))
                }
                .buttonStyle(.plain)
            }
            if participants.isEmpty {
                Text("task_details_participants_none")
                    .textStyle(typography.bodyMedium)
                    .foregroundStyle(Color(argb: colors.textMuted))
            } else {
                FlowLayout(spacing: CGFloat(spacing.space8)) {
                    ForEach(participants, id: \.userId) { member in
                        ParticipantChip(member: member)
                    }
                }
            }
        }
    }
}

private struct ParticipantChip: View {
    let member: TaskDetailsMember
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        HStack(spacing: CGFloat(spacing.space8)) {
            MemberAvatar(initials: member.initials, colorHex: member.avatarColorHex, size: chipAvatarSize)
            Text(member.name)
                .textStyle(DesignTokens.shared.typography.labelMedium)
                .foregroundStyle(Color(argb: colors.textOnAccent))
        }
        .padding(.horizontal, CGFloat(spacing.space8))
        .padding(.vertical, CGFloat(spacing.space4))
        .background(Color(argb: colors.accentDim), in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8)))
    }
}

/// Multi-select picker of the production's assignable members, searched by name. Each tap saves
/// straight away (the ViewModel's `ParticipantToggled`); rows are disabled while a save is running.
struct ParticipantPickerSheet: View {
    let model: TaskDetailsModel
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let selectedIds = Set(state.participants.map(\.userId))
        VStack(spacing: CGFloat(spacing.space12)) {
            InputField(
                placeholder: "task_details_participants_search_placeholder",
                text: Binding(
                    get: { model.state.participantQuery },
                    set: { model.onIntent(TaskDetailsIntentParticipantSearchChanged(query: $0)) }
                ),
                systemImage: "magnifyingglass"
            )
            .autocorrectionDisabled()
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(state.filteredAssignableMembers, id: \.userId) { member in
                        ParticipantOptionRow(member: member, isSelected: selectedIds.contains(member.userId)) {
                            model.onIntent(TaskDetailsIntentParticipantToggled(userId: member.userId))
                        }
                        .disabled(state.isUpdatingParticipants)
                    }
                    if state.filteredAssignableMembers.isEmpty && !state.participantQuery.trimmingCharacters(in: .whitespaces).isEmpty {
                        Text("task_details_participants_no_results")
                            .textStyle(DesignTokens.shared.typography.bodySmall)
                            .foregroundStyle(Color(argb: colors.textMuted))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, CGFloat(spacing.space12))
                    }
                }
            }
        }
        .padding(.horizontal, CGFloat(spacing.space16))
        .padding(.top, CGFloat(spacing.space24))
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
        .presentationBackground(Color(argb: colors.cardBackground))
    }
}

private struct ParticipantOptionRow: View {
    let member: TaskDetailsMember
    let isSelected: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        Button(action: onTap) {
            HStack(spacing: CGFloat(spacing.space12)) {
                MemberAvatar(initials: member.initials, colorHex: member.avatarColorHex, size: rowAvatarSize)
                Text(member.name)
                    .textStyle(DesignTokens.shared.typography.bodyLarge)
                    .foregroundStyle(Color(argb: isSelected ? colors.textOnAccent : colors.textPrimary))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, CGFloat(spacing.space8))
            .padding(.vertical, CGFloat(spacing.space12))
            .background(isSelected ? Color(argb: colors.accentDim) : .clear, in: shape)
            .contentShape(shape)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}
