import SwiftUI
import FrameZero

// The production-details cards; each mirrors the composeApp/features/production-details component of
// the same name.

private let barHeight: CGFloat = 6
private let phaseDotSize: CGFloat = 16
private let statusDotSize: CGFloat = 8
private let crewAvatarSize: CGFloat = 44
private let countBadgePaddingHorizontal: CGFloat = 6
private let countBadgePaddingVertical: CGFloat = 2

/// Quoted logline (when set), overall progress bar and the members / days left / budget stats.
struct LoglineCard: View {
    let detail: ProductionDetailUi
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let phaseColor = Color(argb: detail.phase.accentColor(in: colors))
        VStack(alignment: .leading, spacing: 0) {
            if let logline = detail.logline, !logline.trimmingCharacters(in: .whitespaces).isEmpty {
                Text(verbatim: "\"\(logline)\"")
                    .textStyle(typography.bodyMedium)
                    .italic()
                    .foregroundStyle(Color(argb: colors.textSecondary))
                Divider()
                    .overlay(Color(argb: colors.border))
                    .padding(.vertical, CGFloat(spacing.space16))
            }
            HStack {
                Text("overall_progress")
                    .textStyle(typography.caption)
                    .foregroundStyle(Color(argb: colors.textMuted))
                Spacer()
                Text(verbatim: "\(detail.progressPercent)%")
                    .textStyle(typography.labelMedium)
                    .foregroundStyle(phaseColor)
            }
            .padding(.bottom, CGFloat(spacing.space8))
            GradientProgressBar(percent: detail.progressPercent, from: phaseColor, to: Color(argb: colors.accent))
                .padding(.bottom, CGFloat(spacing.space16))
            HStack(spacing: CGFloat(spacing.space8)) {
                StatItem(value: "\(detail.membersCount)", label: "stat_members")
                StatItem(value: "\(detail.daysLeft)d", label: "stat_days_left")
                StatItem(value: detail.budgetLabel, label: "stat_budget")
            }
        }
        .detailCard(colors, bordered: true)
    }
}

private struct GradientProgressBar: View {
    let percent: Int32
    let from: Color
    let to: Color
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4))
        let fraction = CGFloat(min(max(percent, 0), 100)) / 100
        shape
            .fill(Color(argb: ColorTokens.forScheme(colorScheme).border))
            .frame(height: barHeight)
            .overlay(alignment: .leading) {
                GeometryReader { proxy in
                    shape
                        .fill(LinearGradient(colors: [from, to], startPoint: .leading, endPoint: .trailing))
                        .frame(width: proxy.size.width * fraction)
                }
            }
            .accessibilityHidden(true) // The percentage beside the label says the same.
    }
}

private struct StatItem: View {
    let value: String
    let label: LocalizedStringKey
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
            Text(verbatim: value)
                .textStyle(DesignTokens.shared.typography.titleMedium)
                .fontWeight(.bold)
                .foregroundStyle(Color(argb: colors.textPrimary))
                .lineLimit(1)
                .minimumScaleFactor(0.7)
            Text(label)
                .textStyle(DesignTokens.shared.typography.caption)
                .foregroundStyle(Color(argb: colors.textMuted))
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, CGFloat(DesignTokens.shared.spacing.space8))
        .padding(.vertical, CGFloat(DesignTokens.shared.spacing.space16))
        .overlay(shape.stroke(Color(argb: colors.cardBorder), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
        .accessibilityElement(children: .combine)
    }
}

/// "Phase n of m", the current phase, and one bar segment per phase (filled up to the current one).
struct PipelineCard: View {
    let pipeline: [ProductionPipelinePhaseUi]
    let currentPhase: ProductionPhase
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let currentIndex = pipeline.firstIndex { $0.isCurrent } ?? -1
        VStack(alignment: .leading, spacing: CGFloat(spacing.space16)) {
            HStack {
                Text("pipeline_header")
                    .textStyle(typography.caption)
                    .foregroundStyle(Color(argb: colors.textMuted))
                    .accessibilityAddTraits(.isHeader)
                Spacer()
                Text(String(format: String(localized: "pipeline_phase_progress"), currentIndex + 1, pipeline.count))
                    .textStyle(typography.bodySmall)
                    .foregroundStyle(Color(argb: colors.textMuted))
            }
            HStack(spacing: CGFloat(spacing.space8)) {
                Circle()
                    .fill(Color(argb: currentPhase.accentColor(in: colors)))
                    .frame(width: phaseDotSize, height: phaseDotSize)
                    .accessibilityHidden(true)
                Text(verbatim: currentPhase.displayLabel)
                    .textStyle(typography.titleLarge)
                    .foregroundStyle(Color(argb: colors.textPrimary))
            }
            HStack(spacing: CGFloat(spacing.space4)) {
                ForEach(pipeline, id: \.phase) { phase in
                    RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4))
                        .fill(Color(argb: phase.isCompleted || phase.isCurrent ? phase.phase.accentColor(in: colors) : colors.border))
                        .frame(height: barHeight)
                }
            }
            .accessibilityHidden(true) // "Phase n of m" says the same.
        }
        .detailCard(colors, bordered: true)
    }
}

/// Start and wrap dates side by side.
struct DateCards: View {
    let startDate: String
    let wrapDate: String

    var body: some View {
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            DateCard(label: "start_date", date: startDate)
            DateCard(label: "wrap_date", date: wrapDate)
        }
    }
}

private struct DateCard: View {
    let label: LocalizedStringKey
    let date: String
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Text(label)
                .textStyle(DesignTokens.shared.typography.caption)
                .foregroundStyle(Color(argb: colors.textMuted))
            Text(verbatim: date)
                .textStyle(DesignTokens.shared.typography.titleMedium)
                .foregroundStyle(Color(argb: colors.textPrimary))
        }
        .detailCard(colors, bordered: true)
        .accessibilityElement(children: .combine)
    }
}

/// The viewer's role, who they report to, their peers and their direct reports.
struct TeamCard: View {
    let viewerCrew: ViewerCrewUi
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        VStack(alignment: .leading, spacing: CGFloat(spacing.space16)) {
            HStack {
                Text("team_header")
                    .textStyle(typography.caption)
                    .fontWeight(.bold)
                    .foregroundStyle(Color(argb: colors.textMuted))
                    .accessibilityAddTraits(.isHeader)
                Spacer()
                Text(String(format: String(localized: "team_viewer_label"), viewerCrew.viewerRole))
                    .textStyle(typography.bodySmall)
                    .fontWeight(.medium)
                    .foregroundStyle(Color(argb: colors.accentText))
                    .padding(.horizontal, CGFloat(spacing.space8))
                    .padding(.vertical, CGFloat(spacing.space4))
                    .background(
                        Color(argb: colors.accentSurface),
                        in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
                    )
            }
            // Sections are separated by a divider, as on Android; the last one has none below it.
            let sections = [
                viewerCrew.manager.map { TeamSection(label: "team_reports_to", count: nil, members: [$0]) },
                viewerCrew.peers.isEmpty ? nil : TeamSection(label: "team_peers", count: viewerCrew.peers.count, members: viewerCrew.peers),
                viewerCrew.reports.isEmpty ? nil : TeamSection(label: "team_direct_reports", count: viewerCrew.reports.count, members: viewerCrew.reports),
            ].compactMap { $0 }
            ForEach(Array(sections.enumerated()), id: \.offset) { index, section in
                section
                if index < sections.count - 1 {
                    Divider().overlay(Color(argb: colors.border))
                }
            }
        }
        .detailCard(colors, bordered: false)
    }
}

private struct TeamSection: View {
    let label: LocalizedStringKey
    /// Shown in a badge beside the label; nil for the single "reports to" row.
    let count: Int?
    let members: [ProductionMemberUi]
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let caption = DesignTokens.shared.typography.caption
        VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
            HStack(spacing: CGFloat(spacing.space8)) {
                Text(label)
                    .textStyle(caption)
                    .fontWeight(count == nil ? .bold : nil)
                if let count {
                    Text(verbatim: "\(count)")
                        .textStyle(caption)
                        .padding(.horizontal, countBadgePaddingHorizontal)
                        .padding(.vertical, countBadgePaddingVertical)
                        .background(
                            Color(argb: colors.border),
                            in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4))
                        )
                }
            }
            .foregroundStyle(Color(argb: colors.textMuted))
            .accessibilityElement(children: .combine)
            ForEach(members, id: \.id) { CrewRow(member: $0) }
        }
    }
}

private struct CrewRow: View {
    let member: ProductionMemberUi
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
            MemberAvatar(
                initials: member.initials,
                colorHex: member.avatarColorHex,
                size: crewAvatarSize,
                style: DesignTokens.shared.typography.labelMedium
            )
            VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
                Text(member.name)
                    .textStyle(DesignTokens.shared.typography.titleMedium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                Text(member.role)
                    .textStyle(DesignTokens.shared.typography.bodySmall)
                    .foregroundStyle(Color(argb: colors.textMuted))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            // Decorative, as on Android: crew rows aren't tappable yet.
            Image(systemName: "chevron.right")
                .foregroundStyle(Color(argb: colors.textPrimary))
                .accessibilityHidden(true)
        }
        .accessibilityElement(children: .combine)
    }
}

/// The production's tasks (done ones struck through) with the "+ Add" button that opens create task.
struct TasksCard: View {
    let tasks: [ProductionTaskUi]
    let onAddTask: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        VStack(alignment: .leading, spacing: CGFloat(spacing.space16)) {
            HStack {
                Text("tasks_header")
                    .textStyle(typography.titleMedium)
                    .fontWeight(.bold)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityAddTraits(.isHeader)
                Spacer()
                AddTaskButton(onTap: onAddTask)
            }
            if tasks.isEmpty {
                Text("tasks_empty")
                    .textStyle(typography.bodySmall)
                    .foregroundStyle(Color(argb: colors.textMuted))
            } else {
                VStack(alignment: .leading, spacing: CGFloat(spacing.space12)) {
                    ForEach(tasks, id: \.id) { TaskRow(task: $0) }
                }
            }
        }
        .detailCard(colors, bordered: false)
    }
}

private struct TaskRow: View {
    let task: ProductionTaskUi
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space12)) {
            Circle()
                .fill(Color(argb: task.isDone ? colors.successText : colors.accent))
                .frame(width: statusDotSize, height: statusDotSize)
                .accessibilityHidden(true)
            Text(task.title)
                .textStyle(DesignTokens.shared.typography.bodyLarge)
                .strikethrough(task.isDone)
                .foregroundStyle(Color(argb: task.isDone ? colors.textMuted : colors.textPrimary))
                .frame(maxWidth: .infinity, alignment: .leading)
            if let dueDateLabel = task.dueDateLabel {
                Text(dueDateLabel)
                    .textStyle(DesignTokens.shared.typography.bodySmall)
                    .foregroundStyle(Color(argb: colors.textMuted))
            }
        }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(task.isDone ? .isSelected : [])
    }
}

private struct AddTaskButton: View {
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        Button(action: onTap) {
            HStack(spacing: CGFloat(spacing.space8)) {
                Image(systemName: "plus")
                    .accessibilityHidden(true)
                Text("tasks_add")
                    .textStyle(DesignTokens.shared.typography.labelLarge)
            }
            .foregroundStyle(Color(argb: colors.textOnAccent))
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.vertical, CGFloat(spacing.space8))
            .background(Color(argb: colors.accent), in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8)))
        }
        .buttonStyle(.plain)
        // Just "Add", which create-task.yaml taps by text; the "+" is decoration.
        .accessibilityLabel(Text("tasks_add"))
    }
}

/// Mirrors `PendingUploadBanner`: the newest background task upload for this production, still
/// uploading (no actions) or failed (Retry / Dismiss).
struct PendingUploadBanner: View {
    let upload: PendingUploadUi
    let onRetry: () -> Void
    let onDismiss: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let content = Color(argb: upload.isFailed ? colors.errorText : colors.accentText)
        let titleKey = upload.isFailed ? "pending_upload_failed_title" : "pending_upload_uploading_title"
        HStack(spacing: CGFloat(spacing.space4)) {
            Text(String(format: String(localized: String.LocalizationValue(titleKey)), upload.taskTitle))
                .textStyle(typography.bodyMedium)
                .foregroundStyle(content)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
            if upload.isFailed {
                BannerAction(title: "pending_upload_retry", color: content, weight: .medium, action: onRetry)
                    .padding(.leading, CGFloat(spacing.space8))
                BannerAction(title: "pending_upload_dismiss", color: Color(argb: colors.textMuted), action: onDismiss)
            }
        }
        .padding(CGFloat(spacing.space16))
        .background(
            Color(argb: upload.isFailed ? colors.errorSurface : colors.accentSurface),
            in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius14))
        )
    }
}

private struct BannerAction: View {
    let title: LocalizedStringKey
    let color: Color
    var weight: Font.Weight?
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .textStyle(DesignTokens.shared.typography.bodyMedium)
                .fontWeight(weight)
                .foregroundStyle(color)
                .padding(.horizontal, CGFloat(DesignTokens.shared.spacing.space8))
                .padding(.vertical, CGFloat(DesignTokens.shared.spacing.space4))
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

private extension View {
    /// The detail cards' padded, rounded card background; some of them add a hairline border.
    func detailCard(_ colors: ColorTokens, bordered: Bool) -> some View {
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        return padding(CGFloat(DesignTokens.shared.spacing.space16))
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(argb: colors.cardBackground), in: shape)
            .overlay {
                if bordered {
                    shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline))
                }
            }
    }
}
