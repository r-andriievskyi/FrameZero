/// Every destination pushed on top of Home. Mirrors `RootComponent.Config` (composeApp/commonMain)
/// minus the roots (Splash/Auth/Home), which `AppRoot` owns.
enum Route: Hashable {
    case account
    case gallery
    case createProduction
    case productionDetails(productionId: String)
    case taskDetails(taskId: String)
    case createTask(productionId: String, productionTitle: String)
    case tasksList
}
