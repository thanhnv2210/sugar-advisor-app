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
}
