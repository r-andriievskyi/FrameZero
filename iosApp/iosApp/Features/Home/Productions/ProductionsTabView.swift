import SwiftUI
import FrameZero

typealias ProductionsTabModel = PagedListModel<ProductionUi>

extension ProductionsTabModel {
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.productionsTabViewModel
        self.init(viewModel, items: viewModel.productions)
    }
}

private let addButtonSize: CGFloat = 40
private let contentFade = 0.15

private enum ContentState { case skeleton, error, empty, list }

/// The Productions tab: header with a create button, then skeleton / error / empty state / paged
/// cards. Mirrors composeApp/features/home `ProductionsContent`; accessibility identifiers =
/// `ProductionsTabTestTags`.
struct ProductionsTabView: View {
    let model: ProductionsTabModel
    let onProductionTap: (String) -> Void
    let onCreateProduction: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let contentState: ContentState =
            if state.isInitialLoad { .skeleton }
            else if state.initialError != nil { .error }
            else if state.isEmpty { .empty }
            else { .list }
        VStack(spacing: CGFloat(spacing.space16)) {
            HStack {
                Text("projects_title")
                    .textStyle(DesignTokens.shared.typography.displayMedium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityAddTraits(.isHeader)
                Spacer()
                // The empty and error states carry their own call to action.
                if contentState != .empty && contentState != .error {
                    AddProductionButton(action: onCreateProduction)
                        .transition(.opacity)
                }
            }
            .frame(minHeight: addButtonSize)
            .padding(.horizontal, CGFloat(spacing.space16))

            Group {
                switch contentState {
                case .skeleton:
                    ScrollView {
                        ProductionsSkeleton().padding(.horizontal, CGFloat(spacing.space16))
                    }
                    .scrollDisabled(true)
                    .accessibilityIdentifier("productions:skeleton")
                case .error:
                    FullScreenError(message: state.initialError?.localized ?? String(localized: "error_generic_message")) {
                        model.refresh()
                    }
                    .accessibilityIdentifier("productions:error")
                case .empty:
                    ScrollView {
                        ProductionsEmptyState(onCreateProduction: onCreateProduction)
                            .padding(.horizontal, CGFloat(spacing.space16))
                    }
                    .accessibilityIdentifier("productions:empty")
                case .list:
                    ProductionsList(model: model, onProductionTap: onProductionTap)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .transition(.opacity)
        }
        .padding(.top, CGFloat(spacing.space16))
        .animation(.easeInOut(duration: contentFade), value: contentState)
        .background(Color(argb: colors.background))
        .task { await model.observe() }
    }
}

private struct ProductionsList: View {
    let model: ProductionsTabModel
    let onProductionTap: (String) -> Void

    var body: some View {
        let state = model.state
        let spacing = DesignTokens.shared.spacing
        ScrollView {
            LazyVStack(spacing: CGFloat(spacing.space16)) {
                if state.isRefreshing {
                    RefreshingHeader(count: state.items.count)
                }
                ForEach(Array(state.items.enumerated()), id: \.element.id) { index, production in
                    ProductionCard(production: production) { onProductionTap(production.id) }
                        .onAppear { model.onItemAppear(at: index) }
                }
                PagingAppendFooter(append: state.append) { model.retry() }
            }
            .padding(.horizontal, CGFloat(spacing.space16))
        }
        // Paging owns the reload; the header above shows it, so the gesture returns at once.
        .refreshable { model.refresh() }
        .accessibilityIdentifier("productions:list")
    }
}

/// Inline reload indicator; mirrors the `DefaultInlineRefreshIndicator` Android shows while
/// refreshing.
private struct RefreshingHeader: View {
    let count: Int
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let typography = DesignTokens.shared.typography
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
            ProgressView()
                .tint(Color(argb: colors.accent))
            Text("projects_refreshing")
                .textStyle(typography.labelMedium)
                .foregroundStyle(Color(argb: colors.textSecondary))
            Text(String(format: String(localized: "projects_count"), count))
                .textStyle(typography.bodySmall)
                .foregroundStyle(Color(argb: colors.textMuted))
        }
        .accessibilityElement(children: .combine)
    }
}

private struct AddProductionButton: View {
    let action: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        Button(action: action) {
            Image(systemName: "plus")
                .fontWeight(.semibold)
                .foregroundStyle(Color(argb: colors.textOnAccent))
                .frame(width: addButtonSize, height: addButtonSize)
                .background(
                    Color(argb: colors.accent),
                    in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
                )
        }
        .buttonStyle(.plain)
        // Android leaves this icon-only button unlabelled; VoiceOver needs a name.
        .accessibilityLabel(Text("projects_create_button"))
    }
}
