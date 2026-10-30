import SwiftUI
import FrameZero

// Liquid Glass (iOS 26) for the navigation layer: chrome that floats over content, never the
// content itself. This is the one place that branches on `#available(iOS 26, *)`; below iOS 26
// each helper falls back to the opaque look the app had before glass. See
// docs/ios-liquid-glass-plan.md.

extension View {
    /// Liquid Glass in `shape` on iOS 26+. Below that, the elevated surface with a hairline border
    /// that glass replaces.
    func glassSurface<S: Shape>(in shape: S) -> some View {
        modifier(GlassSurface(shape: shape))
    }

    /// `background` below iOS 26 only, for separation glass provides by itself (e.g. a scrim behind
    /// floating chrome).
    @ViewBuilder
    func preGlassBackground<V: View>(@ViewBuilder _ background: () -> V) -> some View {
        if #available(iOS 26, *) {
            self
        } else {
            self.background(content: background)
        }
    }

    /// A bar pinned to the bottom edge, e.g. a form's call to action. On iOS 26+ it's a `safeAreaBar`:
    /// content scrolls under it behind the system scroll-edge effect. Below that, a `safeAreaInset` on
    /// the opaque `background`, with a `border` divider on top when `divider` is set.
    func glassBottomBar<Bar: View>(
        spacing: CGFloat? = nil,
        divider: Bool = false,
        @ViewBuilder _ bar: () -> Bar
    ) -> some View {
        modifier(GlassBottomBar(spacing: spacing, divider: divider, bar: bar()))
    }

    /// Sheet background: the system's Liquid Glass on iOS 26+ (glass at partial detents, opaque at
    /// `.large`), `cardBackground` below. Overriding it on 26 would turn glass off at every detent.
    func glassSheetBackground() -> some View {
        modifier(GlassSheetBackground())
    }

    /// `.glass` / `.glassProminent` on iOS 26+, `.bordered` / `.borderedProminent` below. All four
    /// honour `tint`.
    @ViewBuilder
    func glassButtonStyle(prominent: Bool = false) -> some View {
        if #available(iOS 26, *) {
            if prominent { buttonStyle(.glassProminent) } else { buttonStyle(.glass) }
        } else {
            if prominent { buttonStyle(.borderedProminent) } else { buttonStyle(.bordered) }
        }
    }
}

extension ToolbarContent {
    /// Drops the shared glass capsule iOS 26 puts behind toolbar items, for an item that is a label
    /// (e.g. a title) rather than a control.
    @ToolbarContentBuilder
    func withoutGlassBackground() -> some ToolbarContent {
        if #available(iOS 26, *) {
            sharedBackgroundVisibility(.hidden)
        } else {
            self
        }
    }
}

private struct GlassBottomBar<Bar: View>: ViewModifier {
    let spacing: CGFloat?
    let divider: Bool
    let bar: Bar
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        if #available(iOS 26, *) {
            content.safeAreaBar(edge: .bottom, spacing: spacing) { bar }
        } else {
            let colors = ColorTokens.forScheme(colorScheme)
            content.safeAreaInset(edge: .bottom, spacing: spacing) {
                VStack(spacing: 0) {
                    if divider {
                        Divider().overlay(Color(argb: colors.border))
                    }
                    bar
                }
                .background(Color(argb: colors.background))
            }
        }
    }
}

private struct GlassSheetBackground: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        if #available(iOS 26, *) {
            content
        } else {
            content.presentationBackground(Color(argb: ColorTokens.forScheme(colorScheme).cardBackground))
        }
    }
}

private struct GlassSurface<S: Shape>: ViewModifier {
    let shape: S
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        if #available(iOS 26, *) {
            content.glassEffect(.regular, in: shape)
        } else {
            let colors = ColorTokens.forScheme(colorScheme)
            content
                .background(Color(argb: colors.surfaceElevated), in: shape)
                .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
        }
    }
}
