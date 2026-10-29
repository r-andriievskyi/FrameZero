import SwiftUI
import FrameZero

/// The signed-in stack: Home at the root, every `Route` pushed on top. Routes stay placeholders
/// until their chunk lands (docs/ios-swiftui-migration.md).
struct SignedInView: View {
    let graph: IosAppGraph
    @State private var path: [Route] = []
    @State private var models = RouteModels<Route>()

    var body: some View {
        NavigationStack(path: $path) {
            HomeView(graph: graph) { path.append($0) }
                .navigationDestination(for: Route.self, destination: screen)
        }
        .onChange(of: path) { _, path in
            models.retain(path)
        }
    }

    @ViewBuilder
    private func screen(_ route: Route) -> some View {
        switch route {
        case .account:
            AccountView(
                model: models.model(for: route) { AccountModel(graph: graph) },
                onDeveloperOptions: { path.append(.gallery) }
            )
        default:
            NotMigratedView(name: String(describing: route))
        }
    }
}
