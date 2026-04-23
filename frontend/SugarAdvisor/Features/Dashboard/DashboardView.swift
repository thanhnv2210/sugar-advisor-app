import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var session: UserSession
    @StateObject private var vm = DashboardViewModel()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    sugarBudgetCard
                    todayConsumptionList
                }
                .padding()
            }
            .navigationTitle("Dashboard")
            .task { await reload() }
            .refreshable { await reload() }
            .overlay {
                if vm.isLoading { ProgressView() }
            }
        }
    }

    // MARK: - Sugar Budget Card
    private var sugarBudgetCard: some View {
        VStack(spacing: 12) {
            HStack {
                Text("Today's Sugar")
                    .font(.headline)
                Spacer()
                Text("\(vm.totalSugarToday, specifier: "%.1f")g / \(vm.dailyLimit, specifier: "%.0f")g")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            ProgressView(value: vm.progress)
                .tint(vm.progressColor)
                .scaleEffect(x: 1, y: 2)

            HStack {
                if vm.isExceeded {
                    Label("Daily limit exceeded!", systemImage: "exclamationmark.triangle.fill")
                        .foregroundColor(.red)
                        .font(.subheadline)
                } else {
                    Label("\(vm.remaining, specifier: "%.1f")g remaining", systemImage: "checkmark.circle.fill")
                        .foregroundColor(.green)
                        .font(.subheadline)
                }
                Spacer()
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Today's Consumptions
    private var todayConsumptionList: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Today's Intake")
                .font(.headline)

            if vm.consumptions.isEmpty {
                Text("No items logged today")
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding()
            } else {
                ForEach(vm.consumptions) { item in
                    ConsumptionRow(item: item)
                }
            }
        }
    }

    private func reload() async {
        guard let userId = session.userId else { return }
        await vm.load(userId: userId)
    }
}

struct ConsumptionRow: View {
    let item: ConsumptionResponse

    var body: some View {
        HStack {
            Image(systemName: "drop.fill")
                .foregroundColor(.green)
            Text("\(item.sugarAmount, specifier: "%.1f")g sugar")
                .font(.body)
            Spacer()
            Text(formattedTime)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var formattedTime: String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = formatter.date(from: item.consumedAt) {
            let display = DateFormatter()
            display.timeStyle = .short
            return display.string(from: date)
        }
        return ""
    }
}
