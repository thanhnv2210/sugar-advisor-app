import Foundation
import Combine

@MainActor
class HistoryViewModel: ObservableObject {
    @Published var consumptions: [ConsumptionResponse] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func load(userId: UUID) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            consumptions = try await APIClient.shared.get("/consumptions/\(userId)?size=100")
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func delete(id: UUID, userId: UUID) async {
        do {
            try await APIClient.shared.delete("/consumptions/\(id)")
            consumptions.removeAll { $0.id == id }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
