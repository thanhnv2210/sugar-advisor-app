import Foundation

@MainActor
class UserSession: ObservableObject {
    static let shared = UserSession()

    @Published var userId: UUID?
    @Published var isReady = false

    private let userIdKey = "sugar_advisor_user_id"

    private init() {}

    func bootstrap() async {
        if let stored = UserDefaults.standard.string(forKey: userIdKey),
           let uuid = UUID(uuidString: stored) {
            userId = uuid
            isReady = true
            return
        }
        await createAndStoreUser()
    }

    private func createAndStoreUser() async {
        do {
            let request = CreateUserRequest(name: "Me", age: nil, weight: nil)
            let user: UserResponse = try await APIClient.shared.post("/users", body: request)
            store(userId: user.id)
        } catch {
            // Retry on next launch — app stays on LaunchView
            print("Failed to create user: \(error)")
        }
    }

    private func store(userId: UUID) {
        self.userId = userId
        self.isReady = true
        UserDefaults.standard.set(userId.uuidString, forKey: userIdKey)
    }
}
