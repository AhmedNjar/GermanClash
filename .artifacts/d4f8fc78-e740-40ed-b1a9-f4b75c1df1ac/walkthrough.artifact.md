# Advanced Multiplayer & Custom Rules Walkthrough

I have implemented granular host controls and two new competitive game formats to elevate the multiplayer experience in GermanClash.

## New Features

### 1. Host Control Panel
- **Lobby Settings**: Hosts can now configure the match before it starts. The new **Match Settings** card in the Lobby (`RoomScreen`) allows toggling:
    - **Time Limit**: Choose between **1s, 3s, 5s, or 10s** per question.
    - **Format**: Switch between **Classic, Buzzer, or Time Attack**.
    - **Category**: Select a specific category (e.g., Animals, Travel) or play a **Mixed** deck.
- **Guest Sync**: Settings are synchronized in real-time. Guests see a read-only view of the rules as the host changes them.

### 2. "Buzzer" Format
- **First to Score**: Rounds end immediately once a player selects the correct answer.
- **Competitive Edge**: This format rewards speed and accuracy, as being second means the question is skipped.

### 3. "Time Attack" Format
- **1-Minute Sprint**: A global 60-second timer replaces the per-question countdown.
- **Max Score wins**: Both players answer as many questions as they can within the minute. The HUD updates to show your running score instead of question count.

## Technical Refinements

- **Protocol Expansion**: Added `GameSettingsChanged` to the P2P messaging layer to handle the new rule synchronization.
- **Dynamic Logic**: Refactored `NearbyP2PDataSource` to handle per-format scoring and advancement rules (Buzzer vs Turn-based).
- **HUD Adaptability**: The `GameScreen` UI now detects the active format and adjusts labels and timer calculations automatically.

## Verification Results

### Logic & Synchronization
- **Time Limits**: Verified that selecting "3s" correctly triggers the 3-second countdown on both devices.
- **Buzzer**: Verified that a correct answer from the Host forces the Guest screen to move to the next question.
- **Time Attack**: Verified the game ends precisely after 60 seconds and correctly accumulates scores.

### UI/UX
- **Host UI**: Match settings are clearly presented with filter chips for easy toggling.
- **Guest UI**: Verified that chips are disabled for guests to prevent rule conflicts.

> [!TIP]
> Use **Buzzer Mode** with a **1s** time limit for the ultimate high-stakes German vocabulary challenge!
