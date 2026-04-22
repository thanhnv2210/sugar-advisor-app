import SwiftUI

@main
struct SugarAdvisorApp: App {
    @StateObject private var session = UserSession.shared

    var body: some Scene {
        WindowGroup {
            Group {
                if session.isReady {
                    ContentView()
                } else {
                    LaunchView()
                }
            }
            .environmentObject(session)
        }
    }
}
