import Foundation

class APIClient {
    static let shared = APIClient()

    // For simulator use localhost. For physical device, replace with your Mac's local IP.
    // e.g. "http://192.168.1.x:8080/api"
    private let baseURL = "http://192.168.1.3:8080/api"

    private let decoder: JSONDecoder = {
        let d = JSONDecoder()
        d.keyDecodingStrategy = .convertFromSnakeCase
        return d
    }()

    private let encoder: JSONEncoder = {
        let e = JSONEncoder()
        e.keyEncodingStrategy = .convertToSnakeCase
        return e
    }()

    private init() {}

    func get<T: Decodable>(_ path: String) async throws -> T {
        let url = try makeURL(path)
        do {
            let (data, response) = try await URLSession.shared.data(from: url)
            let code = (response as? HTTPURLResponse)?.statusCode
            await APILogger.shared.log(method: "GET", url: url.absoluteString, requestBody: nil,
                                       statusCode: code, responseBody: String(data: data, encoding: .utf8), error: nil)
            try validate(response)
            return try decoder.decode(T.self, from: data)
        } catch {
            await APILogger.shared.log(method: "GET", url: url.absoluteString, requestBody: nil,
                                       statusCode: nil, responseBody: nil, error: error.localizedDescription)
            throw error
        }
    }

    func post<T: Decodable, B: Encodable>(_ path: String, body: B) async throws -> T {
        let url = try makeURL(path)
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        let bodyData = try encoder.encode(body)
        request.httpBody = bodyData
        let requestBodyString = String(data: bodyData, encoding: .utf8)
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            let code = (response as? HTTPURLResponse)?.statusCode
            await APILogger.shared.log(method: "POST", url: url.absoluteString, requestBody: requestBodyString,
                                       statusCode: code, responseBody: String(data: data, encoding: .utf8), error: nil)
            try validate(response)
            return try decoder.decode(T.self, from: data)
        } catch {
            await APILogger.shared.log(method: "POST", url: url.absoluteString, requestBody: requestBodyString,
                                       statusCode: nil, responseBody: nil, error: error.localizedDescription)
            throw error
        }
    }

    private func makeURL(_ path: String) throws -> URL {
        guard let url = URL(string: baseURL + path) else {
            throw APIError.invalidURL
        }
        return url
    }

    private func validate(_ response: URLResponse) throws {
        guard let http = response as? HTTPURLResponse else { return }
        guard (200...299).contains(http.statusCode) else {
            throw APIError.httpError(http.statusCode)
        }
    }
}

enum APIError: LocalizedError {
    case invalidURL
    case httpError(Int)

    var errorDescription: String? {
        switch self {
        case .invalidURL: return "Invalid URL"
        case .httpError(404): return "Not found"
        case .httpError(let code): return "Server error (\(code))"
        }
    }
}
