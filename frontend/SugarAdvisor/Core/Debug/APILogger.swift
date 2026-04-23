import Foundation
import Combine

struct APILogEntry: Identifiable {
    let id = UUID()
    let timestamp: Date
    let method: String
    let url: String
    let requestBody: String?
    let statusCode: Int?
    let responseBody: String?
    let error: String?

    var isSuccess: Bool { (statusCode ?? 0) >= 200 && (statusCode ?? 0) < 300 }

    var formattedTime: String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm:ss.SSS"
        return f.string(from: timestamp)
    }
}

@MainActor
class APILogger: ObservableObject {
    static let shared = APILogger()
    @Published var entries: [APILogEntry] = []

    private init() {}

    func log(method: String,
             url: String,
             requestBody: String?,
             statusCode: Int?,
             responseBody: String?,
             error: String?) {
        let entry = APILogEntry(
            timestamp: Date(),
            method: method,
            url: url,
            requestBody: requestBody,
            statusCode: statusCode,
            responseBody: responseBody,
            error: error
        )
        entries.insert(entry, at: 0) // newest first
    }

    func clear() { entries.removeAll() }

    func exportText() -> String {
        let header = "Sugar Advisor — API Log\nExported: \(Date())\n" + String(repeating: "=", count: 60)
        let body = entries.map { e -> String in
            var lines: [String] = []
            lines.append("\n[\(e.formattedTime)] \(e.method) \(e.url)")
            if let code = e.statusCode { lines.append("Status : \(code)") }
            if let req  = e.requestBody  { lines.append("Request:\n\(prettyPrint(req))") }
            if let res  = e.responseBody { lines.append("Response:\n\(prettyPrint(res))") }
            if let err  = e.error        { lines.append("Error  : \(err)") }
            return lines.joined(separator: "\n")
        }.joined(separator: "\n" + String(repeating: "-", count: 60))
        return header + body
    }

    private func prettyPrint(_ raw: String) -> String {
        guard let data = raw.data(using: .utf8),
              let obj  = try? JSONSerialization.jsonObject(with: data),
              let pretty = try? JSONSerialization.data(withJSONObject: obj, options: .prettyPrinted),
              let str  = String(data: pretty, encoding: .utf8) else { return raw }
        return str
    }
}
