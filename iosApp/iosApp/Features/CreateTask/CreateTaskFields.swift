import SwiftUI
import FrameZero

// The create-task form controls; each mirrors the composeApp/features/task-create component of the
// same name.

private let fieldHeight: CGFloat = 48
private let assigneeFieldHeight: CGFloat = 56
private let assigneeAvatarSize: CGFloat = 32
private let dashLength: CGFloat = 6
private let dashGap: CGFloat = 4
private let dashStroke: CGFloat = 1.5

/// Dashed "attach a file" prompt, or the picked file's name with Remove.
struct AttachmentRow: View {
    let attachmentName: String?
    let onAttach: () -> Void
    let onRemove: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius14))
        if let attachmentName {
            HStack(spacing: CGFloat(spacing.space12)) {
                PaperclipIcon(color: colors.accent, background: colors.accentSurface)
                Text(attachmentName)
                    .textStyle(typography.titleSmall)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .lineLimit(1)
                    .truncationMode(.middle)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Button(action: onRemove) {
                    Text("attachment_remove")
                        .textStyle(typography.bodyMedium)
                        .fontWeight(.medium)
                        .foregroundStyle(Color(argb: colors.errorText))
                        .padding(.horizontal, CGFloat(spacing.space8))
                        .padding(.vertical, CGFloat(spacing.space4))
                }
                .buttonStyle(.plain)
            }
            .padding(CGFloat(spacing.space16))
            .background(Color(argb: colors.inputBackground), in: shape)
        } else {
            Button(action: onAttach) {
                HStack(spacing: CGFloat(spacing.space12)) {
                    PaperclipIcon(color: colors.textMuted, background: colors.inputBackground)
                    VStack(alignment: .leading, spacing: CGFloat(spacing.space4)) {
                        Text("attach_file_title")
                            .textStyle(typography.titleSmall)
                            .foregroundStyle(Color(argb: colors.textPrimary))
                        Text("attach_file_subtitle")
                            .textStyle(typography.bodySmall)
                            .foregroundStyle(Color(argb: colors.textMuted))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    Image(systemName: "plus")
                        .foregroundStyle(Color(argb: colors.textMuted))
                        .accessibilityHidden(true)
                }
                .padding(CGFloat(spacing.space16))
                .overlay(
                    shape.inset(by: dashStroke / 2).stroke(
                        Color(argb: colors.border),
                        style: StrokeStyle(lineWidth: dashStroke, dash: [dashLength, dashGap])
                    )
                )
                .contentShape(shape)
            }
            .buttonStyle(.plain)
        }
    }
}

private struct PaperclipIcon: View {
    let color: Int64
    let background: Int64

    var body: some View {
        Image(systemName: "paperclip")
            .foregroundStyle(Color(argb: color))
            .padding(CGFloat(DesignTokens.shared.spacing.space8))
            .background(Color(argb: background), in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8)))
            .accessibilityHidden(true)
    }
}

/// The chosen assignee (avatar + name) or the "Assign to someone" prompt; tap opens the picker.
struct AssigneeField: View {
    let assignee: AssignableMemberUi?
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        Button(action: onTap) {
            HStack(spacing: CGFloat(spacing.space12)) {
                if let assignee {
                    MemberAvatar(initials: assignee.initials, colorHex: assignee.avatarColorHex, size: assigneeAvatarSize)
                    Text(assignee.name)
                        .foregroundStyle(Color(argb: colors.textPrimary))
                } else {
                    Text("assignee_unassigned")
                        .foregroundStyle(Color(argb: colors.textMuted))
                }
                Spacer(minLength: 0)
            }
            .textStyle(DesignTokens.shared.typography.bodyLarge)
            .padding(.horizontal, CGFloat(spacing.space16))
            .frame(height: assigneeFieldHeight)
            .fieldBackground(colors)
        }
        .buttonStyle(.plain)
    }
}

/// "Add participants" pill at the end of the participant chips.
struct AddParticipantsButton: View {
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        Button(action: onTap) {
            Text("participants_add")
                .textStyle(DesignTokens.shared.typography.bodySmall)
                .foregroundStyle(Color(argb: colors.textSecondary))
                .padding(.horizontal, CGFloat(spacing.space12))
                .padding(.vertical, CGFloat(spacing.space8))
                .fieldBackground(colors)
        }
        .buttonStyle(.plain)
    }
}

/// High / Medium / Low, each tinted in its priority colors when selected.
struct PrioritySelector: View {
    let selected: TaskPriority
    let onSelect: (TaskPriority) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let options: [(TaskPriority, LocalizedStringKey, Int64, Int64)] = [
            (.high, "priority_high", colors.priorityHighSurface, colors.priorityHighText),
            (.medium, "priority_medium", colors.priorityMedSurface, colors.priorityMedText),
            (.low, "priority_low", colors.priorityLowSurface, colors.priorityLowText),
        ]
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space12)) {
            ForEach(options, id: \.0) { priority, label, surface, text in
                let isSelected = priority == selected
                let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
                Button { onSelect(priority) } label: {
                    Text(label)
                        .textStyle(DesignTokens.shared.typography.labelLarge)
                        .foregroundStyle(Color(argb: isSelected ? text : colors.textPrimary))
                        .frame(maxWidth: .infinity, minHeight: fieldHeight)
                        .background(Color(argb: isSelected ? surface : colors.inputBackground), in: shape)
                        .overlay(
                            shape.stroke(
                                Color(argb: isSelected ? text : colors.border),
                                lineWidth: CGFloat(DesignTokens.shared.border.hairline)
                            )
                        )
                        .contentShape(shape)
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
    }
}

/// Quick-pick chips plus the dd.mm.yyyy calendar field.
struct DueDateSection: View {
    let dueDate: Kotlinx_datetimeLocalDate?
    let onQuickSelect: (DueDateQuickOption) -> Void
    let onDateChange: (Kotlinx_datetimeLocalDate?) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let quickOptions: [(DueDateQuickOption, LocalizedStringKey)] = [
            (.today, "due_today"), (.tomorrow, "due_tomorrow"), (.thisWeek, "due_this_week"), (.nextWeek, "due_next_week"),
        ]
        VStack(alignment: .leading, spacing: CGFloat(spacing.space12)) {
            FlowLayout(spacing: CGFloat(spacing.space8)) {
                ForEach(quickOptions, id: \.0) { option, label in
                    Button { onQuickSelect(option) } label: {
                        Text(label)
                            .textStyle(DesignTokens.shared.typography.labelMedium)
                            .foregroundStyle(Color(argb: colors.textPrimary))
                            .padding(.horizontal, CGFloat(spacing.space16))
                            .padding(.vertical, CGFloat(spacing.space8))
                            .fieldBackground(colors, radius: DesignTokens.shared.radius.radiusMax)
                    }
                    .buttonStyle(.plain)
                }
            }
            DateField(date: dueDate, placeholder: "due_date_placeholder", label: "label_due_date") { onDateChange($0) }
        }
    }
}

private extension View {
    /// The form fields' input-background fill with a hairline border.
    func fieldBackground(_ colors: ColorTokens, radius: Float = DesignTokens.shared.radius.radius8) -> some View {
        let shape = RoundedRectangle(cornerRadius: CGFloat(radius))
        return background(Color(argb: colors.inputBackground), in: shape)
            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
            .contentShape(shape)
    }
}
