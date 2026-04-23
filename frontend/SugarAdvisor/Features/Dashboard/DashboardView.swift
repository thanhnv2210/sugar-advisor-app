import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var session: UserSession
    @StateObject private var vm = DashboardViewModel()
    @State private var showingLogSheet = false

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
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showingLogSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingLogSheet) {
                LogIntakeSheet { sugarAmount in
                    guard let userId = session.userId else { return }
                    try await vm.logManual(userId: userId, sugarAmount: sugarAmount)
                }
            }
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

// MARK: - Log Intake Sheet

struct LogIntakeSheet: View {
    let onLog: (Double) async throws -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var sugarText = ""
    @State private var isLogging = false
    @State private var errorMessage: String?

    private var sugarAmount: Double? {
        Double(sugarText.replacingOccurrences(of: ",", with: "."))
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack {
                        TextField("e.g. 12.5", text: $sugarText)
                            .keyboardType(.decimalPad)
                        Text("g")
                            .foregroundColor(.secondary)
                    }
                } header: {
                    Text("Sugar amount")
                } footer: {
                    Text("Enter the sugar content of what you consumed.")
                }

                if let error = errorMessage {
                    Section {
                        Label(error, systemImage: "exclamationmark.circle.fill")
                            .foregroundColor(.red)
                            .font(.subheadline)
                    }
                }
            }
            .navigationTitle("Log Intake")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Log") {
                        guard let amount = sugarAmount, amount > 0 else {
                            errorMessage = "Enter a valid sugar amount."
                            return
                        }
                        isLogging = true
                        errorMessage = nil
                        Task {
                            do {
                                try await onLog(amount)
                                dismiss()
                            } catch {
                                errorMessage = error.localizedDescription
                            }
                            isLogging = false
                        }
                    }
                    .disabled(sugarText.isEmpty || isLogging)
                }
            }
        }
        .presentationDetents([.medium])
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
