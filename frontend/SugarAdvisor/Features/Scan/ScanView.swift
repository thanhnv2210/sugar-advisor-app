import SwiftUI

struct ScanView: View {
    @EnvironmentObject var session: UserSession
    @StateObject private var vm = ScanViewModel()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    barcodeInputCard
                    if let product = vm.product {
                        productCard(product)
                        logCard
                    }
                    if let analysis = vm.analysis {
                        analysisCard(analysis)
                    }
                    if let error = vm.errorMessage {
                        errorCard(error)
                    }
                }
                .padding()
            }
            .navigationTitle("Scan")
        }
    }

    // MARK: - Barcode Input
    private var barcodeInputCard: some View {
        VStack(spacing: 12) {
            HStack {
                Image(systemName: "barcode")
                    .foregroundColor(.secondary)
                TextField("Enter barcode", text: $vm.barcode)
                    .keyboardType(.numberPad)
                    .submitLabel(.search)
                    .onSubmit { Task { await vm.lookupProduct() } }
                if !vm.barcode.isEmpty {
                    Button { vm.reset() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding()
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12))

            Button {
                Task { await vm.lookupProduct() }
            } label: {
                Label(vm.isLookingUp ? "Looking up…" : "Look Up", systemImage: "magnifyingglass")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.green)
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .disabled(vm.barcode.isEmpty || vm.isLookingUp)
        }
    }

    // MARK: - Product Card
    private func productCard(_ product: ProductResponse) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(product.name ?? "Unknown product")
                .font(.headline)
            if let brand = product.brand {
                Text(brand)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            Divider()
            HStack {
                Label("\(product.sugarPer100g ?? 0, specifier: "%.1f")g per 100g", systemImage: "drop.fill")
                    .foregroundColor(.green)
                Spacer()
            }
            if let ingredients = product.ingredients {
                Text(ingredients)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(3)
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Serving + Log
    private var logCard: some View {
        VStack(spacing: 12) {
            HStack {
                Text("Serving size")
                    .font(.subheadline)
                Spacer()
                TextField("grams", text: $vm.servingGrams)
                    .keyboardType(.decimalPad)
                    .multilineTextAlignment(.trailing)
                    .frame(width: 70)
                Text("g")
                    .foregroundColor(.secondary)
            }
            Divider()
            HStack {
                Text("Sugar intake")
                    .font(.subheadline)
                Spacer()
                Text("\(vm.sugarAmount, specifier: "%.1f")g")
                    .font(.headline)
                    .foregroundColor(.green)
            }
            Button {
                guard let userId = session.userId else { return }
                Task { await vm.logConsumption(userId: userId) }
            } label: {
                Label(vm.isLogging ? "Logging…" : "Log Consumption", systemImage: "plus.circle.fill")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(vm.logSuccess ? Color.gray : Color.green)
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .disabled(vm.isLogging || vm.logSuccess)
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Analysis Result
    private func analysisCard(_ analysis: SugarAnalysisResponse) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Analysis")
                    .font(.headline)
                Spacer()
                SugarLevelBadge(level: analysis.sugarLevel)
            }
            Text(analysis.recommendation)
                .font(.subheadline)
                .foregroundColor(.secondary)
            if analysis.isExceedLimit {
                Label("Exceeds your daily limit", systemImage: "exclamationmark.triangle.fill")
                    .foregroundColor(.red)
                    .font(.caption)
            } else {
                Label("\(analysis.remainingSugar, specifier: "%.1f")g remaining today", systemImage: "checkmark.circle.fill")
                    .foregroundColor(.green)
                    .font(.caption)
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Error
    private func errorCard(_ message: String) -> some View {
        HStack {
            Image(systemName: "exclamationmark.circle.fill")
                .foregroundColor(.red)
            Text(message)
                .font(.subheadline)
                .foregroundColor(.red)
        }
        .padding()
        .background(Color.red.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct SugarLevelBadge: View {
    let level: String

    var color: Color {
        switch level {
        case "LOW": return .green
        case "MEDIUM": return .orange
        default: return .red
        }
    }

    var body: some View {
        Text(level)
            .font(.caption)
            .fontWeight(.semibold)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(color.opacity(0.2))
            .foregroundColor(color)
            .clipShape(Capsule())
    }
}
