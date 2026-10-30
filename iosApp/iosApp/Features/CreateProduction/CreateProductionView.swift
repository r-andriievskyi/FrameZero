import SwiftUI
import FrameZero

typealias CreateProductionModel = ScreenModel<CreateProductionState, any CreateProductionIntent, any CreateProductionEvent>

extension CreateProductionModel {
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.createProductionViewModel
        self.init(viewModel, state: viewModel.state, events: viewModel.events, onIntent: CreateProductionViewModel.onIntent)
    }
}

private let stepDotSize: CGFloat = 12
private let stepDotActiveWidth: CGFloat = 24

/// The three-step wizard (details → crew → review) in one route; mirrors composeApp/features/production
/// `CreateProductionScreen`. Back belongs to the ViewModel, as on Android: the toolbar's back button
/// sends `BackPressed`, which steps back or, on step 1, emits `Dismissed`. The system back button (and
/// with it swipe-back) is hidden so it can't skip that. `Created` and `Dismissed` both pop the route.
struct CreateProductionView: View {
    let model: CreateProductionModel
    /// Slide direction for the step change being animated.
    @State private var isForward = true
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        VStack(spacing: 0) {
            Text(String(format: String(localized: "create_step_indicator"), Int(state.currentStep), Int(state.totalSteps)))
                .textStyle(DesignTokens.shared.typography.bodyMedium)
                .foregroundStyle(Color(argb: colors.textMuted))
                .padding(.bottom, CGFloat(spacing.space8))
            StepIndicator(current: state.currentStep, total: state.totalSteps)
                .padding(.bottom, CGFloat(spacing.space24))
            ZStack {
                step(state.currentStep)
                    .id(state.currentStep)
                    .transition(.push(from: isForward ? .trailing : .leading))
                    .accessibilityIdentifier("create-production:step:\(state.currentStep)")
            }
            .frame(maxHeight: .infinity, alignment: .top)
            .clipped()
        }
        .background(Color(argb: colors.background))
        .navigationTitle("create_title")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden()
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button { send(CreateProductionIntentBackPressed.shared, forward: false) } label: {
                    Image(systemName: "chevron.backward")
                }
                .accessibilityLabel(Text("cd_back"))
            }
        }
        .errorToast(state.errorToast?.localized) { model.onIntent(CreateProductionIntentToastDismissed.shared) }
        .task { await model.observe() }
        .task { await model.handleEvents { _ in dismiss() } }
    }

    @ViewBuilder
    private func step(_ step: Int32) -> some View {
        switch step {
        case 1: DetailsStep(model: model) { send(CreateProductionIntentNextStep.shared, forward: true) }
        case 2: CrewStep(model: model) { send(CreateProductionIntentNextStep.shared, forward: true) }
        default: ReviewStep(model: model)
        }
    }

    /// Sends a step-changing intent inside an animation, so the step slides in the right direction.
    private func send(_ intent: any CreateProductionIntent, forward: Bool) {
        isForward = forward
        withAnimation(.easeInOut(duration: 0.3)) { model.onIntent(intent) }
    }
}

/// Mirrors `StepIndicator` (CreateProductionTopBar): filled dots up to the current step, which is wider.
private struct StepIndicator: View {
    let current: Int32
    let total: Int32
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
            ForEach(1...Int(total), id: \.self) { index in
                Capsule()
                    .fill(Color(argb: index <= current ? colors.accent : colors.cardBackground))
                    .frame(width: index == current ? stepDotActiveWidth : stepDotSize, height: stepDotSize)
            }
        }
        .accessibilityHidden(true) // The "Step n of m" line above says the same.
    }
}

/// Scrolling step body: heading, subtitle, fields, then the step's error and call to action.
struct WizardStep<Content: View>: View {
    let title: LocalizedStringKey
    let subtitle: LocalizedStringKey
    @ViewBuilder let content: Content
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        ScrollView {
            VStack(alignment: .leading, spacing: CGFloat(spacing.space24)) {
                VStack(alignment: .leading, spacing: CGFloat(spacing.space4)) {
                    Text(title)
                        .textStyle(DesignTokens.shared.typography.displayMedium)
                        .foregroundStyle(Color(argb: colors.textPrimary))
                        .accessibilityAddTraits(.isHeader)
                    Text(subtitle)
                        .textStyle(DesignTokens.shared.typography.bodyMedium)
                        .foregroundStyle(Color(argb: colors.textSecondary))
                }
                content
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.bottom, CGFloat(spacing.space24))
        }
        .scrollDismissesKeyboard(.interactively)
    }
}

/// Uppercase field caption; mirrors production's `FieldLabel`.
struct FieldLabel: View {
    let text: Text
    @Environment(\.colorScheme) private var colorScheme

    init(_ key: LocalizedStringKey) {
        text = Text(key)
    }

    init(verbatim: String) {
        text = Text(verbatim: verbatim)
    }

    var body: some View {
        text
            .textStyle(DesignTokens.shared.typography.labelSmall)
            .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textSecondary))
    }
}
