# Sugar Advisor — iOS Frontend

SwiftUI app targeting iOS 17+, tested on iPhone 16 / iOS 26.

## Xcode Setup (one-time)

1. Open Xcode → **File > New > Project**
2. Choose **iOS → App**
3. Fill in:
   - Product Name: `SugarAdvisor`
   - Bundle Identifier: `com.yourname.sugaradvisor`
   - Interface: **SwiftUI**
   - Language: **Swift**
   - Minimum Deployments: **iOS 17**
4. Save into `frontend/` — Xcode will create `frontend/SugarAdvisor.xcodeproj`
5. Delete the auto-generated `ContentView.swift` and `<AppName>App.swift` files
6. In Xcode, right-click the `SugarAdvisor` group → **Add Files to "SugarAdvisor"**
7. Select all files from `frontend/SugarAdvisor/` and add them

## Running on Simulator

Make sure the backend is running (`./gradlew bootRun` in `backend/`), then press **Run** in Xcode. The app connects to `http://localhost:8080/api` by default.

## Running on Physical Device (iPhone)

The device and Mac must be on the same Wi-Fi network. Update the base URL in `APIClient.swift`:

```swift
// Replace with your Mac's local IP address
private let baseURL = "http://192.168.x.x:8080/api"
```

Find your Mac's IP: **System Settings → Wi-Fi → Details**.

Also add to `Info.plist` to allow HTTP (non-HTTPS) connections:
```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

## Project Structure

```
SugarAdvisor/
├── App/
│   ├── SugarAdvisorApp.swift   entry point, bootstraps user session
│   └── ContentView.swift       TabView (Dashboard / Scan / History)
├── Core/
│   ├── Network/
│   │   └── APIClient.swift     URLSession wrapper with async/await
│   ├── Models/
│   │   └── Models.swift        Codable request/response types
│   └── Session/
│       └── UserSession.swift   stores userId in UserDefaults, creates user on first launch
└── Features/
    ├── Launch/
    │   └── LaunchView.swift    shown while bootstrapping user session
    ├── Dashboard/
    │   ├── DashboardView.swift   today's sugar total, progress bar, intake list
    │   └── DashboardViewModel.swift
    ├── Scan/
    │   ├── ScanView.swift        barcode input, product info, log consumption, analysis result
    │   └── ScanViewModel.swift
    └── History/
        ├── HistoryView.swift     full consumption history list
        └── HistoryViewModel.swift
```

## User Flow

```
First launch → POST /api/users → store UUID in UserDefaults
     ↓
Tab Bar
 ├── Dashboard  → GET /api/consumptions/{userId} → show today's total vs 50g limit
 ├── Scan       → GET /api/products/barcode/{barcode}
 │               → POST /api/consumptions
 │               → POST /api/analysis/sugar → show recommendation
 └── History    → GET /api/consumptions/{userId} → full list
```
