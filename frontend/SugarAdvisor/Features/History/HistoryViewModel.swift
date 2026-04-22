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
            consumptions = try await APIClient.shared.get("/consumptions/\(userId)")
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
