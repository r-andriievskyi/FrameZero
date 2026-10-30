import SwiftUI
import FrameZero

private let chipAvatarSize: CGFloat = 24
private let rowAvatarSize: CGFloat = 32

/// A production member as the task screens show one. task-details and task-create each export their
/// own member type (`TaskDetailsMember`, `AssignableMemberUi`) with the same fields.
protocol PickableMember {
    var userId: String { get }
    var name: String { get }
    var initials: String { get }
    var avatarColorHex: String? { get }
}

extension TaskDetailsMember: PickableMember {}
extension AssignableMemberUi: PickableMember {}

/// Avatar + name on an `accentDim` pill; mirrors the participant chips of task-details and task-create.
struct MemberChip: View {
    let member: any PickableMember
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

/// Bottom sheet listing members filtered by a search field — the iOS form of the `ModalBottomSheet`
/// pickers in task-details (participants) and task-create (assignee, participants). Filtering stays in
/// the ViewModel: `members` is already the filtered list for `query`.
///
/// `noneOption` adds a first row that selects nobody (`onSelect(nil)`), for the single-select
/// assignee picker.
struct MemberPickerSheet<Member: PickableMember>: View {
    let searchPlaceholder: LocalizedStringResource
    let noResults: LocalizedStringKey
    @Binding var query: String
    let members: [Member]
    var noneOption: LocalizedStringKey?
    var isSelected: (Member) -> Bool = { _ in false }
    var isDisabled = false
    let onSelect: (String?) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        VStack(spacing: CGFloat(spacing.space12)) {
            InputField(placeholder: searchPlaceholder, text: $query, systemImage: "magnifyingglass")
                .autocorrectionDisabled()
            ScrollView {
                LazyVStack(spacing: 0) {
                    if let noneOption {
                        MemberOptionRow(label: Text(noneOption), member: nil, isSelected: false) { onSelect(nil) }
                    }
                    ForEach(members, id: \.userId) { member in
                        MemberOptionRow(label: Text(member.name), member: member, isSelected: isSelected(member)) {
                            onSelect(member.userId)
                        }
                    }
                    if members.isEmpty && !query.trimmingCharacters(in: .whitespaces).isEmpty {
                        Text(noResults)
                            .textStyle(DesignTokens.shared.typography.bodySmall)
                            .foregroundStyle(Color(argb: colors.textMuted))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, CGFloat(spacing.space12))
                    }
                }
                .disabled(isDisabled)
            }
        }
        .padding(.horizontal, CGFloat(spacing.space16))
        .padding(.top, CGFloat(spacing.space24))
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
        .glassSheetBackground()
    }
}

private struct MemberOptionRow: View {
    let label: Text
    let member: (any PickableMember)?
    let isSelected: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        Button(action: onTap) {
            HStack(spacing: CGFloat(spacing.space12)) {
                if let member {
                    MemberAvatar(initials: member.initials, colorHex: member.avatarColorHex, size: rowAvatarSize)
                }
                label
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
