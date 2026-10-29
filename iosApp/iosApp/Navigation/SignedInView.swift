import SwiftUI
import FrameZero

/// The signed-in stack: Home at the root, every `Route` pushed on top. Routes stay placeholders
/// until their chunk lands (docs/ios-swiftui-migration.md).
struct SignedInView: View {
    let graph: IosAppGraph
    @State private var path: [Route] = []

    var body: some View {
        NavigationStack(path: $path) {
            HomeView(graph: graph) { path.append($0) }
                .navigationDestination(for: Route.self) { route in
                    NotMigratedView(name: String(describing: route))
                }
        }
    }
}
