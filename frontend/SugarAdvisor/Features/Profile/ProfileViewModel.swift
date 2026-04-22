import Foundation
import Combine

@MainActor
class ProfileViewModel: ObservableObject {
    @Published var user: UserResponse?
    @Published var isLoading = false
    @Published var isSaving = false
    @Published var errorMessage: String?
    @Published var saveSuccess = false

    // Editable fields — populated when user loads
    @Published var name = ""
    @Published var ageText = ""
    @Published var weightText = ""
    @Published var dailyLimitText = ""

    func load(userId: UUID) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let loaded: UserResponse = try await APIClient.shared.get("/users/\(userId)")
            user = loaded
            name = loaded.name
            ageText = loaded.age.map { "\($0)" } ?? ""
            weightText = loaded.weight.map { String(format: "%.1f", $0) } ?? ""
            dailyLimitText = loaded.dailySugarLimit.map { String(format: "%.0f", $0) } ?? "50"
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func save(userId: UUID) async {
        isSaving = true
        errorMessage = nil
        saveSuccess = false
        defer { isSaving = false }

        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        guard !trimmedName.isEmpty else {
            errorMessage = "Name cannot be empty"
            return
        }

        let request = UpdateUserRequest(
            name: trimmedName,
            age: Int(ageText),
            weight: Double(weightText),
            dailySugarLimit: Double(dailyLimitText)
        )

        do {
            let updated: UserResponse = try await APIClient.shared.patch("/users/\(userId)", body: request)
            user = updated
            saveSuccess = true
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    var dailyLimit: Double {
        Double(dailyLimitText) ?? 50
    }
}
