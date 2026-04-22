import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var session: UserSession
    @StateObject private var vm = ProfileViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if vm.isLoading {
                    ProgressView("Loading profile…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    form
                }
            }
            .navigationTitle("Profile")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                        guard let userId = session.userId else { return }
                        Task { await vm.save(userId: userId) }
                    } label: {
                        if vm.isSaving {
                            ProgressView().scaleEffect(0.8)
                        } else {
                            Text("Save")
                        }
                    }
                    .disabled(vm.isSaving || vm.isLoading)
                }
            }
            .task {
                guard let userId = session.userId else { return }
                await vm.load(userId: userId)
            }
        }
    }

    // MARK: - Form

    private var form: some View {
        Form {
            // Success banner
            if vm.saveSuccess {
                Section {
                    Label("Profile saved!", systemImage: "checkmark.circle.fill")
                        .foregroundColor(.green)
                }
            }

            // Error banner
            if let error = vm.errorMessage {
                Section {
                    Label(error, systemImage: "exclamationmark.circle.fill")
                        .foregroundColor(.red)
                }
            }

            Section("Personal Info") {
                LabeledContent("Name") {
                    TextField("Your name", text: $vm.name)
                        .multilineTextAlignment(.trailing)
                }

                LabeledContent("Age") {
                    TextField("Optional", text: $vm.ageText)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                }

                LabeledContent("Weight (kg)") {
                    TextField("Optional", text: $vm.weightText)
                        .keyboardType(.decimalPad)
                        .multilineTextAlignment(.trailing)
                }
            }

            Section {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("Daily Sugar Limit")
                            .font(.subheadline)
                        Spacer()
                        TextField("50", text: $vm.dailyLimitText)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 60)
                        Text("g")
                            .foregroundColor(.secondary)
                    }

                    // Preset quick-pick buttons
                    HStack(spacing: 10) {
                        ForEach([25, 37, 50, 75], id: \.self) { preset in
                            Button {
                                vm.dailyLimitText = "\(preset)"
                            } label: {
                                Text("\(preset)g")
                                    .font(.caption)
                                    .fontWeight(.medium)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 5)
                                    .background(vm.dailyLimitText == "\(preset)" ? Color.green : Color(.secondarySystemBackground))
                                    .foregroundColor(vm.dailyLimitText == "\(preset)" ? .white : .primary)
                                    .clipShape(Capsule())
                            }
                        }
                        Spacer()
                    }

                    // Visual sugar limit gauge
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 4)
                                .fill(Color(.systemGray5))
                                .frame(height: 8)
                            RoundedRectangle(cornerRadius: 4)
                                .fill(limitColor)
                                .frame(width: geo.size.width * limitRatio, height: 8)
                                .animation(.easeInOut, value: vm.dailyLimit)
                        }
                    }
                    .frame(height: 8)

                    Text(limitDescription)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding(.vertical, 4)
            } header: {
                Text("Sugar Budget")
            } footer: {
                Text("WHO recommends under 25g free sugars per day. 50g is the upper limit for adults.")
            }

            Section("Account") {
                if let user = vm.user {
                    LabeledContent("User ID", value: user.id.uuidString)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
    }

    // MARK: - Helpers

    private var limitRatio: CGFloat {
        min(vm.dailyLimit / 100.0, 1.0)
    }

    private var limitColor: Color {
        switch vm.dailyLimit {
        case ..<26: return .green
        case 26..<51: return .orange
        default: return .red
        }
    }

    private var limitDescription: String {
        switch vm.dailyLimit {
        case ..<26: return "Strict — WHO optimal target"
        case 26..<51: return "Moderate — WHO upper limit"
        default: return "Lenient — above WHO recommendation"
        }
    }
}
