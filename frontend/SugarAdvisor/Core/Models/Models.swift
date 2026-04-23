import Foundation

// MARK: - User
struct CreateUserRequest: Encodable {
    let name: String
    let age: Int?
    let weight: Double?
}

struct UserResponse: Decodable {
    let id: UUID
    let name: String
    let age: Int?
    let weight: Double?
    let dailySugarLimit: Double?
}

struct UpdateUserRequest: Encodable {
    let name: String?
    let age: Int?
    let weight: Double?
    let dailySugarLimit: Double?
}

// MARK: - Product
struct ProductResponse: Decodable {
    let id: UUID
    let name: String?
    let brand: String?
    let barcode: String?
    let sugarPer100g: Double?
    let ingredients: String?
}

// MARK: - Consumption
struct ConsumptionRequest: Encodable {
    let userId: UUID
    let productId: UUID?
    let sugarAmount: Double
}

struct ConsumptionResponse: Decodable, Identifiable {
    let id: UUID
    let userId: UUID
    let productId: UUID?
    let sugarAmount: Double
    let consumedAt: String
}

// MARK: - Daily Summary
struct DailySummaryResponse: Decodable {
    let totalSugar: Double
    let dailyLimit: Double
    let remaining: Double
    let status: String          // e.g. "WITHIN_LIMIT" / "EXCEEDED"
}

// MARK: - Analysis
struct SugarAnalysisRequest: Encodable {
    let userId: UUID
    let productId: UUID?
    let sugarAmount: Double
}

struct SugarAnalysisResponse: Decodable {
    let sugarLevel: String       // LOW / MEDIUM / HIGH
    let isExceedLimit: Bool
    let remainingSugar: Double
    let recommendation: String
}
