import Foundation
import Combine
import SwiftUI

@MainActor
class DashboardViewModel: ObservableObject {
    @Published var consumptions: [ConsumptionResponse] = []
    @Published var summary: DailySummaryResponse?
    @Published var isLoading = false
    @Published var errorMessage: String?

    var totalSugarToday: Double { summary?.totalSugar ?? 0 }
    var dailyLimit: Double { summary?.dailyLimit ?? 50 }
    var remaining: Double { max(summary?.remaining ?? 50, 0) }
    var isExceeded: Bool { (summary?.status ?? "") == "EXCEEDED" }
    var progress: Double { min(totalSugarToday / max(dailyLimit, 1), 1.0) }
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
            // Fetch summary and today's consumption list in parallel
            async let summaryResult: DailySummaryResponse = APIClient.shared.get("/users/\(userId)/summary/today")
            async let consumptionsResult: [ConsumptionResponse] = APIClient.shared.get("/consumptions/\(userId)")
            let (fetchedSummary, allConsumptions) = try await (summaryResult, consumptionsResult)
            summary = fetchedSummary
            consumptions = todayOnly(allConsumptions)
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
