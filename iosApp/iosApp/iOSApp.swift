import SwiftUI
import FirebaseCore
import FrameZero

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     handleEventsForBackgroundURLSession identifier: String,
                     completionHandler: @escaping () -> Void) {
        IosBackgroundUploadBridge.shared.handleEventsForBackgroundSession(completionHandler: completionHandler)
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @State private var session = SessionModel(graph: FrameZeroFrameworkKt.iosAppGraph())

    init() {
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            RootView(session: session)
        }
    }
}
