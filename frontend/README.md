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
│   ├── SugarAdvisorApp.swift   entry point, shake-to-debug, bootstraps user session
│   └── ContentView.swift       TabView (Dashboard / Scan / History / Profile)
├── Core/
│   ├── Network/
│   │   └── APIClient.swift     URLSession wrapper (GET, POST, PATCH, DELETE) with request/response logging
│   ├── Models/
│   │   └── Models.swift        Codable request/response types
│   ├── Session/
│   │   └── UserSession.swift   stores userId in UserDefaults, creates user on first launch
│   └── Debug/
│       ├── APILogger.swift     in-memory log store (method, URL, req/res body, status, timestamp)
│       └── DebugLogView.swift  shake-triggered log viewer (DEBUG only)
└── Features/
    ├── Launch/
    │   └── LaunchView.swift    shown while bootstrapping user session
    ├── Dashboard/
    │   ├── DashboardView.swift   today's sugar total, progress bar, intake list, manual log sheet
    │   └── DashboardViewModel.swift
    ├── Scan/
    │   ├── ScanView.swift           barcode input + camera scanner, product info, log consumption
    │   ├── ScanViewModel.swift
    │   └── BarcodeScannerView.swift AVFoundation camera scanner (UIViewControllerRepresentable)
    ├── History/
    │   ├── HistoryView.swift        full consumption history list
    │   └── HistoryViewModel.swift
    └── Profile/
        ├── ProfileView.swift        edit name, age, weight, daily sugar limit with preset buttons
        └── ProfileViewModel.swift   GET + PATCH /api/users/{userId}
```

## CLI Build & Deploy

All commands run from the `frontend/` directory.

### Simulator

```bash
# Build
xcodebuild -project SugarAdvisor.xcodeproj -scheme SugarAdvisor \
  -destination 'platform=iOS Simulator,id=E72627DD-965B-4FFA-B203-5E882C023491' build

# Install
xcrun simctl install E72627DD-965B-4FFA-B203-5E882C023491 \
  ~/Library/Developer/Xcode/DerivedData/SugarAdvisor-*/Build/Products/Debug-iphonesimulator/SugarAdvisor.app

# Launch
xcrun simctl launch E72627DD-965B-4FFA-B203-5E882C023491 thanh.nguyen.SugarAdvisor
```

### Physical Device (ThanhNguyen iPhone 16 Pro — iOS 26.4)

```bash
# Build
xcodebuild -project SugarAdvisor.xcodeproj -scheme SugarAdvisor \
  -destination 'platform=iOS,id=00008140-00065D441180801C' -allowProvisioningUpdates build

# Install
xcrun devicectl device install app --device 27B7E6F4-2F84-5CFB-A858-A31ECD350909 \
  ~/Library/Developer/Xcode/DerivedData/SugarAdvisor-*/Build/Products/Debug-iphoneos/SugarAdvisor.app

# Launch
xcrun devicectl device process launch --device 27B7E6F4-2F84-5CFB-A858-A31ECD350909 \
  thanh.nguyen.SugarAdvisor
```

> **Note:** The "No provider was found" warning during install/launch is harmless — it's a provisioning profile lookup quirk and does not affect the app.

### Useful identifiers

| Item | Value |
|------|-------|
| Simulator UDID | `E72627DD-965B-4FFA-B203-5E882C023491` |
| Device UDID (xcodebuild) | `00008140-00065D441180801C` |
| Device UDID (devicectl) | `27B7E6F4-2F84-5CFB-A858-A31ECD350909` |
| Bundle ID | `thanh.nguyen.SugarAdvisor` |
| Team ID | `R6B94B8A4D` |
| Signing | Xcode 26.4.1+ required (`sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`) |

---

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

**Shake to view API logs (DEBUG builds only)**
- Make some API calls, then physically shake the device (or use **Device → Shake** in the simulator menu)
- A log panel appears showing every request and response with body, status code, and timestamp
- Tap any row for full detail with pretty-printed JSON

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
 ├── Dashboard  → GET /api/users/{userId}/summary/today  (sugar total, limit, remaining, status)
 │             → GET /api/consumptions/{userId}?from=today&to=today  (today's list)
 │             → [+] button → LogIntakeSheet
 │                            → POST /api/consumptions  (productId: nil, manual sugar amount)
 ├── Scan       → camera scanner or manual barcode entry
 │             → GET /api/products/barcode/{barcode}
 │             → POST /api/consumptions
 │             → POST /api/analysis/sugar → show recommendation
 ├── History    → GET /api/consumptions/{userId}?size=100  (full list, newest first)
 │             → swipe left on row → DELETE /api/consumptions/{id}
 └── Profile    → GET /api/users/{userId} → display profile
               → PATCH /api/users/{userId} → save name, age, weight, daily limit
```
