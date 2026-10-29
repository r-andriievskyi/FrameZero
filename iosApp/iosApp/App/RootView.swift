import SwiftUI

struct RootView: View {
    let session: SessionModel

    var body: some View {
        Group {
            switch session.root {
            case .splash:
                SplashView()
            case .auth:
                AuthFlowView(graph: session.graph)
            case .home:
                SignedInView(graph: session.graph)
            }
        }
        .task { await session.run() }
    }
}
