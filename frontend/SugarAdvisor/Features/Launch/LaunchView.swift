import SwiftUI

struct LaunchView: View {
    @EnvironmentObject var session: UserSession

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "drop.fill")
                .font(.system(size: 64))
                .foregroundColor(.green)

            Text("Sugar Advisor")
                .font(.largeTitle)
                .fontWeight(.bold)

            ProgressView()
                .padding(.top, 8)

            Text("Setting up your profile…")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .task {
            await session.bootstrap()
        }
    }
}
