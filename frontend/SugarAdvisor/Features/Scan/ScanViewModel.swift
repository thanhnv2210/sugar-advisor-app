import Foundation
import Combine

@MainActor
class ScanViewModel: ObservableObject {
    @Published var barcode = ""
    @Published var servingGrams = "100"
    @Published var product: ProductResponse?
    @Published var analysis: SugarAnalysisResponse?
    @Published var isLookingUp = false
    @Published var isLogging = false
    @Published var errorMessage: String?
    @Published var logSuccess = false

    var sugarAmount: Double {
        guard let sugar = product?.sugarPer100g,
              let grams = Double(servingGrams) else { return 0 }
        return sugar * grams / 100
    }

    func lookupProduct() async {
        guard !barcode.isEmpty else { return }
        isLookingUp = true
        product = nil
        analysis = nil
        errorMessage = nil
        logSuccess = false
        defer { isLookingUp = false }

        do {
            product = try await APIClient.shared.get("/products/barcode/\(barcode)")
        } catch APIError.httpError(404) {
            errorMessage = "Product not found for barcode \"\(barcode)\""
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func logConsumption(userId: UUID) async {
        guard let product else { return }
        isLogging = true
        errorMessage = nil
        defer { isLogging = false }

        do {
            let consumptionRequest = ConsumptionRequest(
                userId: userId,
                productId: product.id,
                sugarAmount: sugarAmount
            )
            let _: ConsumptionResponse = try await APIClient.shared.post("/consumptions", body: consumptionRequest)

            let analysisRequest = SugarAnalysisRequest(
                userId: userId,
                productId: product.id,
                sugarAmount: sugarAmount
            )
            analysis = try await APIClient.shared.post("/analysis/sugar", body: analysisRequest)
            logSuccess = true
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func reset() {
        barcode = ""
        servingGrams = "100"
        product = nil
        analysis = nil
        errorMessage = nil
        logSuccess = false
    }
}
