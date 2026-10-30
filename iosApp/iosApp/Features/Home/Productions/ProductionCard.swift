import SwiftUI
import FrameZero

private let progressBarHeight: CGFloat = 6
/// The countdown turns to the error color from this many days out.
private let urgentDaysLeft: Int32 = 7

/// One production in the Productions tab; mirrors composeApp/features/home `ProductionCard`.
struct ProductionCard: View {
    let production: ProductionUi
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let accent = Color(argb: production.phase.accentColor(in: colors))
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 0) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: CGFloat(spacing.space4)) {
                        Text(production.title)
                            .textStyle(typography.titleMedium)
                            .foregroundStyle(Color(argb: colors.textPrimary))
                        Text(production.genre.displayLabel)
                            .textStyle(typography.bodySmall)
                            .foregroundStyle(Color(argb: colors.textMuted))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    PhaseBadge(phase: production.phase)
                }
                .padding(.bottom, CGFloat(spacing.space16))

                HStack {
                    Text("projects_pipeline_progress")
                        .textStyle(typography.bodySmall)
                        .foregroundStyle(Color(argb: colors.textMuted))
                    Spacer()
                    Text("\(production.progressPercent)%")
                        .textStyle(typography.labelMedium)
                        .foregroundStyle(accent)
                }
                .padding(.bottom, CGFloat(spacing.space8))

                ProgressBar(percent: production.progressPercent, color: accent)
                    .padding(.bottom, CGFloat(spacing.space16))

                HStack(spacing: CGFloat(spacing.space16)) {
                    MetaLabel(
                        systemImage: "person.2",
                        text: String.localizedStringWithFormat(
                            NSLocalizedString("members_count", comment: ""),
                            Int(production.membersCount)
                        ),
                        color: colors.textMuted
                    )
                    // Hidden once the wrap date has passed, where it would read "-35d left".
                    if production.daysLeft > 0 {
                        MetaLabel(
                            systemImage: "calendar.badge.clock",
                            text: String(format: String(localized: "days_left"), Int(production.daysLeft)),
                            color: production.daysLeft <= urgentDaysLeft ? colors.errorText : colors.textMuted
                        )
                    }
                }
            }
            .padding(CGFloat(spacing.space16))
            .background(Color(argb: colors.cardBackground), in: shape)
            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
            .contentShape(shape)
        }
        .buttonStyle(.plain)
    }
}

private struct PhaseBadge: View {
    let phase: ProductionPhase
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        Text(phase.displayLabel)
            .textStyle(DesignTokens.shared.typography.labelSmall)
            .foregroundStyle(Color(argb: colors.accentText))
            .padding(.horizontal, CGFloat(spacing.space8))
            .padding(.vertical, CGFloat(spacing.space4))
            .background(
                Color(argb: colors.accentSurface),
                in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
            )
    }
}

private struct ProgressBar: View {
    let percent: Int32
    let color: Color
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4))
        let fraction = CGFloat(min(max(percent, 0), 100)) / 100
        shape
            .fill(Color(argb: ColorTokens.forScheme(colorScheme).border))
            .frame(height: progressBarHeight)
            .overlay(alignment: .leading) {
                GeometryReader { proxy in
                    shape.fill(color).frame(width: proxy.size.width * fraction)
                }
            }
            .accessibilityHidden(true) // The percentage label above carries the value.
    }
}

private struct MetaLabel: View {
    let systemImage: String
    let text: String
    let color: Int64
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
            Image(systemName: systemImage)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textPrimary))
                .accessibilityHidden(true)
            Text(text)
                .textStyle(DesignTokens.shared.typography.bodySmall)
                .foregroundStyle(Color(argb: color))
        }
    }
}
