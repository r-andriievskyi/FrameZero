/// The screen models of one navigation stack, keyed by route: a model is created the first time
/// its route renders and released (→ the ViewModel's `onDestroy()`) once `retain(_:)` no longer
/// lists the route. Hold it in the stack's `@State` and call `retain` whenever the path changes.
///
/// Deliberately not `@Observable`: models are created while SwiftUI evaluates `body`, which must
/// not mutate observed state.
@MainActor
final class RouteModels<Route: Hashable> {
    private var models: [Route: AnyObject] = [:]

    func model<Model: AnyObject>(for route: Route, make: () -> Model) -> Model {
        if let model = models[route] as? Model {
            return model
        }
        let model = make()
        models[route] = model
        return model
    }

    func retain(_ routes: some Sequence<Route>) {
        let kept = Set(routes)
        models = models.filter { kept.contains($0.key) }
    }
}
