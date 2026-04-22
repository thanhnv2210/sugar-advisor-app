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

## Start, Stop & Debug

### Start the app

1. Open Xcode with:
   ```bash
   open frontend/SugarAdvisor.xcodeproj
   ```
2. In the toolbar, pick a simulator (e.g. **iPhone 16**) from the device selector.
3. Press **▶ Run** or `Cmd + R`. The simulator boots and the app launches automatically.

### Stop the app

- Press **■ Stop** in the toolbar, or `Cmd + .`

### Debug

**View print/log output**
- Open the Debug Area at the bottom: `Cmd + Shift + Y`
- `print("...")` calls from Swift code appear in the **Console** tab there.

**Set a breakpoint**
1. Click any **line number** in a `.swift` file — a blue marker appears.
2. Run the app (`Cmd + R`). Execution pauses at that line.
3. Inspect variables in the Debug Area, then step through code:
   - `F6` — Step Over (next line)
   - `F7` — Step Into (enter a function)
   - `F8` — Continue to next breakpoint

**SwiftUI canvas preview**
- Open any `*View.swift` file → click **Resume** in the canvas panel, or press `Cmd + Option + P`.

**Common errors**

| Error | Fix |
|-------|-----|
| Signing certificate error | Xcode → Project → **Signing & Capabilities** → set your Apple ID as the Team |
| Simulator won't boot | **Window → Devices and Simulators** → restart the simulator |
| Build fails after pulling code | `Cmd + Shift + K` (Clean Build Folder), then `Cmd + R` |
| API calls fail / no data | Start the backend first: `cd backend && ./gradlew bootRun` |

---

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
