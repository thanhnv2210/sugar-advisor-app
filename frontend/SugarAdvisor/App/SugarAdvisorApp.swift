import SwiftUI
import UIKit

// MARK: - Shake detection via first-responder UIViewController

extension Notification.Name {
    static let deviceDidShake = Notification.Name("deviceDidShake")
}

class ShakeViewController: UIViewController {
    override var canBecomeFirstResponder: Bool { true }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        becomeFirstResponder()
    }

    override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        if motion == .motionShake {
            NotificationCenter.default.post(name: .deviceDidShake, object: nil)
        }
    }
}

struct ShakeDetector: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> ShakeViewController { ShakeViewController() }
    func updateUIViewController(_ vc: ShakeViewController, context: Context) {}
}

// MARK: - App

@main
struct SugarAdvisorApp: App {
    @StateObject private var session = UserSession.shared
    @State private var showDebugLog = false

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
            .background(ShakeDetector().frame(width: 0, height: 0))
            .onReceive(NotificationCenter.default.publisher(for: .deviceDidShake)) { _ in
                #if DEBUG
                showDebugLog = true
                #endif
            }
            .sheet(isPresented: $showDebugLog) {
                DebugLogView()
            }
        }
    }
}
