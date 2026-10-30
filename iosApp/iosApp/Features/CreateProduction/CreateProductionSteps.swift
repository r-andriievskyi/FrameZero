import SwiftUI
import FrameZero

// The wizard's three steps; mirror composeApp/features/production `Step1Content` (details),
// `Step2Content` (crew) and `Step3Content` (review).

private let addCrewButtonSize: CGFloat = 48
private let crewAvatarSize: CGFloat = 36

/// Step 1: title, genre, start / wrap dates, optional budget.
struct DetailsStep: View {
    let model: CreateProductionModel
    let onContinue: () -> Void

    var body: some View {
        let state = model.state
        let spacing = DesignTokens.shared.spacing
        WizardStep(title: "create_step1_title", subtitle: "create_step1_subtitle") {
            VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                FieldLabel("create_field_production_title")
                InputField(
                    placeholder: "create_placeholder_production_title",
                    text: Binding(
                        get: { model.state.title },
                        set: { model.onIntent(CreateProductionIntentTitleChanged(title: $0)) }
                    )
                )
                .disabled(state.isLoading)
            }
            VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                FieldLabel("create_field_genre")
                FlowLayout(spacing: CGFloat(spacing.space8)) {
                    ForEach(Genre.allCases, id: \.self) { genre in
                        GenreChip(genre: genre, isSelected: genre == state.genre) {
                            model.onIntent(CreateProductionIntentGenreChanged(genre: genre))
                        }
                    }
                }
            }
            HStack(alignment: .top, spacing: CGFloat(spacing.space8)) {
                VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                    FieldLabel("create_field_start_date")
                    DateField(
                        date: state.startDate,
                        placeholder: "create_placeholder_date",
                        label: "create_field_start_date",
                        systemImage: "calendar",
                        isEnabled: !state.isLoading
                    ) { model.onIntent(CreateProductionIntentStartDateChanged(date: $0)) }
                }
                VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                    FieldLabel("create_field_wrap_date")
                    DateField(
                        date: state.wrapDate,
                        placeholder: "create_placeholder_date",
                        label: "create_field_wrap_date",
                        systemImage: "calendar",
                        isEnabled: !state.isLoading
                    ) { model.onIntent(CreateProductionIntentWrapDateChanged(date: $0)) }
                }
            }
            VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                FieldLabel("create_field_budget")
                InputField(placeholder: "budget_placeholder", text: budget, prefix: "$")
                    .keyboardType(.numberPad)
                    .disabled(state.isLoading)
            }
            StepFooter(error: state.error, title: "create_button_continue", action: onContinue)
        }
    }

    /// Whole dollars in the field, cents in the ViewModel — as Android's `BudgetInputField`: blank
    /// clears the budget, other input keeps only its digits.
    private var budget: Binding<String> {
        Binding(
            get: { model.state.budgetCents.map { String($0.int64Value / 100) } ?? "" },
            set: { raw in
                if raw.trimmingCharacters(in: .whitespaces).isEmpty {
                    model.onIntent(CreateProductionIntentBudgetChanged(budgetCents: nil))
                } else if let dollars = Int64(raw.filter(\.isNumber)) {
                    model.onIntent(CreateProductionIntentBudgetChanged(budgetCents: KotlinLong(value: dollars * 100)))
                }
            }
        )
    }
}

/// Step 2: add crew (name + role), list them, continue or skip.
struct CrewStep: View {
    let model: CreateProductionModel
    let onContinue: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        WizardStep(title: "create_step3_title", subtitle: "create_step3_subtitle") {
            HStack(alignment: .bottom, spacing: CGFloat(spacing.space8)) {
                VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                    FieldLabel("create_field_name")
                    InputField(
                        placeholder: "create_placeholder_name",
                        text: Binding(
                            get: { model.state.crewNameInput },
                            set: { model.onIntent(CreateProductionIntentCrewNameChanged(name: $0)) }
                        )
                    )
                    .textContentType(.name)
                    .disabled(state.isLoading)
                }
                VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                    FieldLabel("create_field_role")
                    RoleMenu(selected: state.crewRoleInput) { model.onIntent(CreateProductionIntentCrewRoleChanged(role: $0)) }
                }
                Button {
                    if !state.crewNameInput.trimmingCharacters(in: .whitespaces).isEmpty {
                        model.onIntent(CreateProductionIntentAddCrewMember.shared)
                    }
                } label: {
                    Image(systemName: "plus")
                        .fontWeight(.semibold)
                        .foregroundStyle(Color(argb: colors.textOnAccent))
                        .frame(width: addCrewButtonSize, height: addCrewButtonSize)
                        .background(
                            Color(argb: colors.accent),
                            in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
                        )
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("cd_add_crew_member"))
            }
            if state.crewMembers.isEmpty {
                VStack(spacing: CGFloat(spacing.space8)) {
                    Text(verbatim: "🎬")
                        .textStyle(DesignTokens.shared.typography.displayLarge)
                        .accessibilityHidden(true)
                    Text("create_crew_empty")
                        .textStyle(DesignTokens.shared.typography.bodyMedium)
                        .foregroundStyle(Color(argb: colors.textMuted))
                }
                .frame(maxWidth: .infinity)
            } else {
                VStack(spacing: CGFloat(spacing.space8)) {
                    // Keyed by position: the ViewModel removes crew by index, and names may repeat.
                    ForEach(Array(state.crewMembers.enumerated()), id: \.offset) { index, member in
                        CrewMemberRow(member: member) {
                            model.onIntent(CreateProductionIntentRemoveCrewMember(index: Int32(index)))
                        }
                    }
                }
            }
            Text("create_crew_owner_note")
                .textStyle(DesignTokens.shared.typography.bodySmall)
                .foregroundStyle(Color(argb: colors.accentText))
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            StepFooter(
                error: state.error,
                title: state.crewMembers.isEmpty ? "create_button_skip_continue" : "create_button_continue",
                action: onContinue
            )
        }
    }
}

/// Step 3: summary card, crew avatars, create.
struct ReviewStep: View {
    let model: CreateProductionModel
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let spacing = DesignTokens.shared.spacing
        WizardStep(title: "create_step4_title", subtitle: "create_step4_subtitle") {
            ReviewCard(state: state)
            if !state.crewMembers.isEmpty {
                VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                    FieldLabel(verbatim: String(format: String(localized: "create_crew_label"), state.crewMembers.count))
                    FlowLayout(spacing: CGFloat(spacing.space8)) {
                        ForEach(Array(state.crewMembers.enumerated()), id: \.offset) { _, member in
                            CrewAvatar(member: member)
                        }
                    }
                }
            }
            // Android swaps the label rather than showing a spinner; taps are ignored while creating.
            StepFooter(
                error: state.error,
                title: state.isLoading ? "create_button_creating" : "create_button_create",
                action: { model.onIntent(CreateProductionIntentSubmit.shared) }
            )
            .disabled(state.isLoading)
        }
    }
}

/// The step's inline error (if any) above its call to action.
private struct StepFooter: View {
    let error: (any UiText)?
    let title: LocalizedStringKey
    let action: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
            if let error {
                ErrorText(error.localized)
            }
            CtaButton(title: title, action: action)
        }
    }
}

private struct GenreChip: View {
    let genre: Genre
    let isSelected: Bool
    var onTap: () -> Void = {}
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let capsule = Capsule()
        Button(action: onTap) {
            Text(genre.displayLabel)
                .textStyle(DesignTokens.shared.typography.labelMedium)
                .foregroundStyle(Color(argb: isSelected ? colors.accentText : colors.textSecondary))
                .padding(.horizontal, CGFloat(spacing.space16))
                .padding(.vertical, CGFloat(spacing.space8))
                .background(Color(argb: isSelected ? colors.accentSurface : colors.cardBackground), in: capsule)
                .overlay(
                    capsule.stroke(
                        Color(argb: isSelected ? colors.accent : colors.cardBorder),
                        lineWidth: CGFloat(DesignTokens.shared.border.hairline)
                    )
                )
                .contentShape(capsule)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

/// Crew role picker over the fixed role list; mirrors production's `RoleDropdown`.
private struct RoleMenu: View {
    let selected: String
    let onSelect: (String) -> Void
    @Environment(\.colorScheme) private var colorScheme

    private static let roles: [String] = [
        "crew_role_director", "crew_role_producer", "crew_role_writer", "crew_role_dp",
        "crew_role_editor", "crew_role_sound", "crew_role_art", "crew_role_other",
    ].map { String(localized: String.LocalizationValue($0)) }

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        Menu {
            ForEach(Self.roles, id: \.self) { role in
                Button { onSelect(role) } label: {
                    if role == selected {
                        Label(role, systemImage: "checkmark")
                    } else {
                        Text(role)
                    }
                }
            }
        } label: {
            HStack {
                Text(selected)
                    .textStyle(DesignTokens.shared.typography.bodyLarge)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .lineLimit(1)
                Spacer(minLength: 0)
                Image(systemName: "chevron.down")
                    .font(.footnote)
                    .foregroundStyle(Color(argb: colors.textMuted))
            }
            .padding(.horizontal, CGFloat(DesignTokens.shared.spacing.space16))
            .frame(height: addCrewButtonSize)
            .background(Color(argb: colors.inputBackground), in: shape)
            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
        }
        .accessibilityLabel(Text("create_field_role"))
        .accessibilityValue(Text(selected))
    }
}

private struct CrewMemberRow: View {
    let member: CrewMemberEntry
    let onRemove: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        HStack(spacing: CGFloat(spacing.space8)) {
            CrewAvatar(member: member)
            VStack(alignment: .leading, spacing: 0) {
                Text(member.name)
                    .textStyle(DesignTokens.shared.typography.bodyMedium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                Text(member.role)
                    .textStyle(DesignTokens.shared.typography.caption)
                    .foregroundStyle(Color(argb: colors.textMuted))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Button(action: onRemove) {
                Image(systemName: "xmark")
                    .font(.footnote)
                    .foregroundStyle(Color(argb: colors.textMuted))
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(String(format: String(localized: "cd_remove_crew_member"), member.name)))
        }
        .padding(.leading, CGFloat(spacing.space8))
        .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
    }
}

/// Initials (shared `CrewMemberEntry.initials`) on an accent circle; mirrors production's `CrewAvatar`.
private struct CrewAvatar: View {
    let member: CrewMemberEntry
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        Text(member.initials)
            .textStyle(DesignTokens.shared.typography.labelSmall)
            .foregroundStyle(Color(argb: colors.textOnAccent))
            .frame(width: crewAvatarSize, height: crewAvatarSize)
            .background(Color(argb: colors.accent), in: Circle())
            .accessibilityHidden(true)
    }
}

/// Summary of step 1 + crew size; mirrors production's `ReviewCard`.
private struct ReviewCard: View {
    let state: CreateProductionState
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        let notSet = String(localized: "create_review_not_set")
        VStack(alignment: .leading, spacing: 0) {
            Group {
                if state.title.trimmingCharacters(in: .whitespaces).isEmpty {
                    Text("create_review_untitled")
                } else {
                    Text(state.title)
                }
            }
            .textStyle(DesignTokens.shared.typography.titleLarge)
            .foregroundStyle(Color(argb: colors.textPrimary))
            .padding(.bottom, CGFloat(spacing.space8))
            GenreChip(genre: state.genre, isSelected: true)
                .allowsHitTesting(false)
                .padding(.bottom, CGFloat(spacing.space16))
            Grid(alignment: .leading, horizontalSpacing: 0, verticalSpacing: CGFloat(spacing.space8)) {
                GridRow {
                    ReviewItem(label: "create_review_start", value: state.startDate?.formatMedium() ?? notSet)
                    ReviewItem(label: "create_review_wrap", value: state.wrapDate?.formatMedium() ?? notSet)
                }
                GridRow {
                    ReviewItem(label: "create_review_budget", value: state.budgetDisplay ?? notSet)
                    // + 1: the creator joins as owner.
                    ReviewItem(
                        label: "create_review_crew",
                        value: String(format: String(localized: "create_review_members_count"), state.crewMembers.count + 1)
                    )
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(CGFloat(spacing.space16))
        .background(Color(argb: colors.cardBackground), in: shape)
        .overlay(shape.stroke(Color(argb: colors.cardBorder), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
    }
}

private struct ReviewItem: View {
    let label: LocalizedStringKey
    let value: String
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        VStack(alignment: .leading, spacing: 0) {
            Text(label)
                .textStyle(DesignTokens.shared.typography.caption)
                .foregroundStyle(Color(argb: colors.textMuted))
            Text(value)
                .textStyle(DesignTokens.shared.typography.bodyMedium)
                .foregroundStyle(Color(argb: colors.textPrimary))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }
}
