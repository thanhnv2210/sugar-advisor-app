import SwiftUI

struct HistoryView: View {
    @EnvironmentObject var session: UserSession
    @StateObject private var vm = HistoryViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if vm.isLoading {
                    ProgressView()
                } else if vm.consumptions.isEmpty {
                    ContentUnavailableView(
                        "No history yet",
                        systemImage: "clock",
                        description: Text("Start scanning products to track your sugar intake.")
                    )
                } else {
                    List(vm.consumptions) { item in
                        HistoryRow(item: item)
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("History")
            .task { await reload() }
            .refreshable { await reload() }
        }
    }

    private func reload() async {
        guard let userId = session.userId else { return }
        await vm.load(userId: userId)
    }
}

struct HistoryRow: View {
    let item: ConsumptionResponse

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("\(item.sugarAmount, specifier: "%.1f")g sugar")
                    .font(.body)
                    .fontWeight(.medium)
                Text(formattedDate)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer()
            Image(systemName: "drop.fill")
                .foregroundColor(sugarColor)
        }
    }

    private var sugarColor: Color {
        switch item.sugarAmount {
        case ..<5: return .green
        case 5..<15: return .orange
        default: return .red
        }
    }

    private var formattedDate: String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = formatter.date(from: item.consumedAt) {
            let display = DateFormatter()
            display.dateStyle = .short
            display.timeStyle = .short
            return display.string(from: date)
        }
        return item.consumedAt
    }
}
