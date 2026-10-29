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
        .task { await consumeDeepLinks() }
    }

    /// This view exists exactly while signed in, so this matches `RootComponent` collecting
    /// `navigationSignal.events` only while logged in. A link emitted earlier waits in the signal's
    /// replay cache; `consume()` clears it so it isn't delivered again.
    private func consumeDeepLinks() async {
        let signal = graph.navigationSignal
        for await deepLink in signal.events {
            switch onEnum(of: deepLink) {
            case .taskDetails(let link):
                path.append(.taskDetails(taskId: link.taskId))
            }
            signal.consume()
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
        case .taskDetails(let taskId):
            TaskDetailsView(model: models.model(for: route) { TaskDetailsModel(graph: graph, taskId: taskId) })
        case .tasksList:
            TasksListView(model: models.model(for: route) { TasksListModel(graph: graph) }) {
                path.append(.taskDetails(taskId: $0))
            }
        case .createTask(let productionId, let productionTitle):
            CreateTaskView(model: models.model(for: route) {
                CreateTaskModel(graph: graph, productionId: productionId, productionTitle: productionTitle)
            })
        case .createProduction:
            CreateProductionView(model: models.model(for: route) { CreateProductionModel(graph: graph) })
        default:
            NotMigratedView(name: String(describing: route))
        }
    }
}
