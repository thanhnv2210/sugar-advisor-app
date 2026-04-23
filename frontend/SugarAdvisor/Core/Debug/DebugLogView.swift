import SwiftUI
import UIKit

struct DebugLogView: View {
    @ObservedObject private var logger = APILogger.shared
    @Environment(\.dismiss) private var dismiss
    @State private var selectedEntry: APILogEntry?
    @State private var shareItem: ShareableText?
    @State private var showingServerConfig = false

    var body: some View {
        NavigationStack {
            Group {
                if logger.entries.isEmpty {
                    ContentUnavailableView("No API calls yet", systemImage: "network", description: Text("Make a request and shake again to see logs."))
                } else {
                    List(logger.entries) { entry in
                        Button { selectedEntry = entry } label: {
                            entryRow(entry)
                        }
                        .foregroundColor(.primary)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("API Logs")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
                ToolbarItem(placement: .primaryAction) {
                    Button { shareItem = ShareableText(logger.exportText()) } label: {
                        Image(systemName: "square.and.arrow.up")
                    }
                    .disabled(logger.entries.isEmpty)
                }
                ToolbarItem(placement: .topBarLeading) {
                    Button { showingServerConfig = true } label: {
                        Image(systemName: "server.rack")
                    }
                }
                ToolbarItem(placement: .destructiveAction) {
                    Button("Clear") { logger.clear() }
                        .foregroundColor(.red)
                        .disabled(logger.entries.isEmpty)
                }
            }
            .sheet(item: $selectedEntry) { entry in
                EntryDetailView(entry: entry)
            }
            .sheet(item: $shareItem) { item in
                ShareSheet(items: [item.value])
            }
            .sheet(isPresented: $showingServerConfig) {
                ServerConfigSheet()
            }
        }
    }

    private func entryRow(_ entry: APILogEntry) -> some View {
        HStack(spacing: 12) {
            // Status indicator
            RoundedRectangle(cornerRadius: 4)
                .fill(statusColor(entry))
                .frame(width: 4)
                .padding(.vertical, 4)

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(entry.method)
                        .font(.caption)
                        .fontWeight(.bold)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(methodColor(entry.method).opacity(0.15))
                        .foregroundColor(methodColor(entry.method))
                        .clipShape(RoundedRectangle(cornerRadius: 4))

                    if let code = entry.statusCode {
                        Text("\(code)")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundColor(statusColor(entry))
                    }

                    Spacer()

                    Text(entry.formattedTime)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }

                Text(entry.url)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)

                if let error = entry.error {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .lineLimit(1)
                }
            }
        }
        .padding(.vertical, 4)
    }

    private func statusColor(_ entry: APILogEntry) -> Color {
        if entry.error != nil { return .red }
        guard let code = entry.statusCode else { return .gray }
        switch code {
        case 200...299: return .green
        case 400...499: return .orange
        default: return .red
        }
    }

    private func methodColor(_ method: String) -> Color {
        switch method {
        case "GET": return .blue
        case "POST": return .green
        default: return .purple
        }
    }
}

// MARK: - Server Config Sheet

struct ServerConfigSheet: View {
    @Environment(\.dismiss) private var dismiss
    @State private var urlText = APIClient.shared.baseURL

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("http://192.168.x.x:8080/api", text: $urlText)
                        .keyboardType(.URL)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                } header: {
                    Text("Backend base URL")
                } footer: {
                    Text("Default: \(APIClient.defaultBaseURL)\nChange takes effect on the next API call.")
                }

                Section {
                    Button("Reset to default") {
                        urlText = APIClient.defaultBaseURL
                    }
                    .foregroundColor(.orange)
                }
            }
            .navigationTitle("Server")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        APIClient.shared.baseURL = urlText.trimmingCharacters(in: .whitespacesAndNewlines)
                        dismiss()
                    }
                    .disabled(urlText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
        .presentationDetents([.medium])
    }
}

// MARK: - Share helpers

struct ShareableText: Identifiable {
    let id = UUID()
    let value: String
    init(_ value: String) { self.value = value }
}

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

// MARK: - Detail View

struct EntryDetailView: View {
    let entry: APILogEntry
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("Request") {
                    labelRow("Method", value: entry.method)
                    labelRow("URL", value: entry.url)
                    if let body = entry.requestBody {
                        jsonBlock(label: "Body", json: body)
                    }
                }

                Section("Response") {
                    if let code = entry.statusCode {
                        labelRow("Status", value: "\(code)")
                    }
                    if let error = entry.error {
                        labelRow("Error", value: error)
                            .foregroundColor(.red)
                    }
                    if let body = entry.responseBody {
                        jsonBlock(label: "Body", json: body)
                    }
                }
            }
            .navigationTitle(entry.formattedTime)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }

    private func labelRow(_ label: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value)
                .font(.subheadline)
                .textSelection(.enabled)
        }
        .padding(.vertical, 2)
    }

    private func jsonBlock(label: String, json: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            ScrollView(.horizontal, showsIndicators: false) {
                Text(prettyPrint(json))
                    .font(.system(.caption, design: .monospaced))
                    .textSelection(.enabled)
            }
        }
        .padding(.vertical, 2)
    }

    private func prettyPrint(_ raw: String) -> String {
        guard let data = raw.data(using: .utf8),
              let obj = try? JSONSerialization.jsonObject(with: data),
              let pretty = try? JSONSerialization.data(withJSONObject: obj, options: .prettyPrinted),
              let str = String(data: pretty, encoding: .utf8) else {
            return raw
        }
        return str
    }
}
