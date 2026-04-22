import Foundation

@MainActor
class DashboardViewModel: ObservableObject {
    @Published var consumptions: [ConsumptionResponse] = []
    @Published var totalSugarToday: Double = 0
    @Published var dailyLimit: Double = 50
    @Published var isLoading = false
    @Published var errorMessage: String?

    var remaining: Double { max(dailyLimit - totalSugarToday, 0) }
    var progress: Double { min(totalSugarToday / dailyLimit, 1.0) }
    var progressColor: Color {
        switch progress {
        case ..<0.5: return .green
        case 0.5..<0.8: return .orange
        default: return .red
        }
    }

    func load(userId: UUID) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let all: [ConsumptionResponse] = try await APIClient.shared.get("/consumptions/\(userId)")
            consumptions = todayOnly(all)
            totalSugarToday = consumptions.reduce(0) { $0 + $1.sugarAmount }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func todayOnly(_ items: [ConsumptionResponse]) -> [ConsumptionResponse] {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        let calendar = Calendar.current
        return items.filter { item in
            if let date = formatter.date(from: item.consumedAt) {
                return calendar.isDateInToday(date)
            }
            return false
        }
    }
}
