import SwiftUI
import UIKit

// MARK: - Shake detection via first-responder UIViewController

extension Notification.Name {
    static let deviceDidShake = Notification.Name("deviceDidShake")
}

class ShakeViewController: UIViewController {
    override var canBecomeFirstResponder: Bool { true }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
        view.isUserInteractionEnabled = false
        // Re-grab first responder whenever the keyboard disappears
        NotificationCenter.default.addObserver(
            self, selector: #selector(regain),
            name: UIResponder.keyboardDidHideNotification, object: nil)
        NotificationCenter.default.addObserver(
            self, selector: #selector(regain),
            name: UIApplication.didBecomeActiveNotification, object: nil)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        becomeFirstResponder()
    }

    @objc private func regain() {
        guard !isFirstResponder else { return }
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
            .overlay(ShakeDetector().allowsHitTesting(false).ignoresSafeArea())
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
