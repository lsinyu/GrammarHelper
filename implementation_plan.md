# GrammarHelper Development Plan

GrammarHelper is a mobile-first, AI-powered grammar assistant designed to go beyond traditional tools by offering real-time teaching, system-wide integration via a Floating Bubble, and personalized analytics.

## User Review Required

> [!IMPORTANT]
> This plan assumes the use of the Gemini API for AI analysis. The user will need to provide a Gemini API key.
> The `AccessibilityService` requires explicit user permission on Android, which must be clearly communicated during onboarding.

## Proposed Changes

The development will follow a phased approach as outlined in the technical blueprint.

---

### Core Architecture & Dependencies

The app follows the MVVM pattern for clean separation of concerns.

#### [NEW] [build.gradle.kts](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/build.gradle.kts)
- Add dependencies for `MPAndroidChart`, `OkHttp`, `Gson`, and Android Navigation Component.

---

### Data Layer (Phase 1)

Implementation of the SQLite persistent storage for sessions, error logs, and chat history.

#### [NEW] [DatabaseHelper.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/database/DatabaseHelper.java)
- Define schema for `user_sessions`, `error_log`, and `chat_history`.

#### [NEW] [DAOs](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/database/)
- `SessionDAO.java`, `ErrorLogDAO.java`, `ChatHistoryDAO.java` for CRUD operations.

---

### AI Integration Layer (Phase 1 & 2)

Connecting the app to the Gemini API for grammar analysis and tutoring.

#### [NEW] [GeminiApiClient.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/ai/GeminiApiClient.java)
- Handle HTTP POST requests to Gemini API.

#### [NEW] [PromptBuilder.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/ai/PromptBuilder.java)
- Construct prompts for Grammar Analysis (A), Error Explanation (B), Text Rewriting (C), Quiz Generation (D), and Tone Detection (E).

---

### UI & Features (Phase 2 & 4)

Building the interactive components of the application.

#### [NEW] [SmartEditorActivity.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/ui/SmartEditorActivity.java)
- Real-time text editing with `SpannableString` underlines (Red for Grammar, Blue for Clarity).
- Context Mode Switcher (Academic, Professional, Casual).

#### [NEW] [ChatbotActivity.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/ui/ChatbotActivity.java)
- Interactive AI tutor using a `RecyclerView` with user and bot chat bubbles.

#### [NEW] [DashboardActivity.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/ui/DashboardActivity.java)
- Visualize progress using `MPAndroidChart` (Grammar Score Over Time, Error Patterns).

---

### System-Wide Integration (Phase 3)

The "Mobile Ctrl+G" innovation using Android services.

#### [NEW] [FloatingBubbleService.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/service/FloatingBubbleService.java)
- System-level overlay using `WindowManager`.

#### [NEW] [GrammarAccessibilityService.java](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/app/src/main/java/com/example/grammarhelper/accessibility/GrammarAccessibilityService.java)
- Read text from external apps and inject fixes.

---

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - Build `PromptBuilderTest.java` to verify JSON payload structure.
    - Build `GrammarResponseParserTest.java` with mock AI responses.
- **Running Tests**: Run via Android Studio's Test runner: `./gradlew test`

### Manual Verification
1. **Editor Test**: Type grammatically incorrect sentences in `SmartEditorActivity` and verify underlines appear. Tap to fix.
2. **Chatbot Test**: Tap "Explain Why" on an error and verify the chatbot provides a clear explanation.
3. **Floating Bubble Test**: Enable accessibility permission, open WhatsApp/Gmail, type an error, and verify the bubble turns red. Apply fix from the overlay panel.
4. **Dashboard Test**: Perform multiple sessions and verify charts in `DashboardActivity` update correctly.
