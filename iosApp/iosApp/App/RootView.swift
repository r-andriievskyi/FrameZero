import SwiftUI

struct RootView: View {
    let session: SessionModel

    var body: some View {
        Group {
            switch session.root {
            case .splash:
                NotMigratedView(name: "Splash")
            case .auth:
                NotMigratedView(name: "Auth")
            case .home:
                SignedInView()
            }
        }
        .task { await session.run() }
    }
}
