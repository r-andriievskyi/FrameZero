import SwiftUI

/// The signed-in stack. Home and every route are placeholders until their chunk lands
/// (docs/ios-swiftui-migration.md); the debug list lets each route be pushed and popped.
struct SignedInView: View {
    @State private var path: [Route] = []

    var body: some View {
        NavigationStack(path: $path) {
            List {
                Section("Not migrated yet: Home") {
                    ForEach(Self.sampleRoutes, id: \.self) { route in
                        NavigationLink(String(describing: route), value: route)
                    }
                }
            }
            .navigationTitle("Home")
            .navigationDestination(for: Route.self) { route in
                NotMigratedView(name: String(describing: route))
            }
        }
    }

    private static let sampleRoutes: [Route] = [
        .account,
        .gallery,
        .createProduction,
        .productionDetails(productionId: "sample"),
        .taskDetails(taskId: "sample"),
        .createTask(productionId: "sample", productionTitle: "Sample"),
        .tasksList
    ]
}
