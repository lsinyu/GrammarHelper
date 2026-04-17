# GrammarHelper
## Project Architecture & Screen Design
### *Group 20 | Technical Blueprint*
> *Based on: GrammarHelper Ultimate Concept Document + Group 20 Proposal*
> *Date: March 2026*

---

## SECTION 1 — PROJECT FOLDER STRUCTURE

The Android project follows the standard **MVVM (Model-View-ViewModel)** pattern for clean separation of logic, UI, and data.

```
GrammarHelper/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/grammarhelper/
│   │   │   │
│   │   │   ├── 📂 ui/                          ← All Screens (Activities & Fragments)
│   │   │   │   ├── MainActivity.java            ← Entry point (Splash + Navigation host)
│   │   │   │   ├── SmartEditorActivity.java     ← Screen 1: Writing Editor
│   │   │   │   ├── ChatbotActivity.java         ← Screen 2: AI Chatbot
│   │   │   │   ├── ErrorReviewActivity.java     ← Screen 3: Error Review Panel
│   │   │   │   ├── DashboardActivity.java       ← Screen 4: Progress Dashboard
│   │   │   │   └── SettingsActivity.java        ← Screen 5: Settings
│   │   │   │
│   │   │   ├── 📂 service/                     ← Background Services
│   │   │   │   ├── FloatingBubbleService.java   ← Floating Bubble overlay service
│   │   │   │   └── GrammarAnalysisService.java  ← Background grammar analysis worker
│   │   │   │
│   │   │   ├── 📂 accessibility/               ← System-Wide Text Reading
│   │   │   │   └── GrammarAccessibilityService.java ← Reads text from other apps
│   │   │   │
│   │   │   ├── 📂 ai/                          ← AI Engine Communication Layer
│   │   │   │   ├── GeminiApiClient.java         ← HTTP client for Gemini API calls
│   │   │   │   ├── PromptBuilder.java           ← Builds different AI prompts (A–E)
│   │   │   │   └── GrammarResponseParser.java   ← Parses JSON response from AI
│   │   │   │
│   │   │   ├── 📂 database/                    ← SQLite Layer
│   │   │   │   ├── DatabaseHelper.java          ← SQLiteOpenHelper (creates all tables)
│   │   │   │   ├── SessionDAO.java              ← CRUD for user_sessions table
│   │   │   │   ├── ErrorLogDAO.java             ← CRUD for error_log table
│   │   │   │   └── ChatHistoryDAO.java          ← CRUD for chat_history table
│   │   │   │
│   │   │   ├── 📂 model/                       ← Data Models (POJOs)
│   │   │   │   ├── GrammarError.java            ← Represents a single grammar error
│   │   │   │   ├── Session.java                 ← Represents a writing session
│   │   │   │   ├── ChatMessage.java             ← Represents one chatbot message
│   │   │   │   └── ErrorPattern.java            ← For Dashboard analytics
│   │   │   │
│   │   │   ├── 📂 adapter/                     ← RecyclerView Adapters
│   │   │   │   ├── ChatAdapter.java             ← Drives the chatbot message list
│   │   │   │   ├── ErrorCardAdapter.java        ← Drives the Error Review Panel list
│   │   │   │   └── BadgeAdapter.java            ← Drives the Dashboard badge grid
│   │   │   │
│   │   │   └── 📂 util/                        ← Utilities & Helpers
│   │   │       ├── TextHighlighter.java         ← Applies SpannableString underlines
│   │   │       ├── ScoreCalculator.java         ← Calculates Grammar/Clarity scores
│   │   │       └── NotificationHelper.java      ← Sends daily grammar tip notifications
│   │   │
│   │   ├── res/
│   │   │   ├── layout/                         ← XML screen layouts
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_smart_editor.xml
│   │   │   │   ├── activity_chatbot.xml
│   │   │   │   ├── activity_error_review.xml
│   │   │   │   ├── activity_dashboard.xml
│   │   │   │   ├── activity_settings.xml
│   │   │   │   ├── overlay_floating_bubble.xml  ← Bubble overlay layout
│   │   │   │   ├── overlay_suggestion_panel.xml ← Mini suggestion panel layout
│   │   │   │   ├── item_chat_user.xml           ← Chat bubble (user side)
│   │   │   │   ├── item_chat_bot.xml            ← Chat bubble (bot side)
│   │   │   │   └── item_error_card.xml          ← Error Review card layout
│   │   │   │
│   │   │   ├── drawable/                       ← Icons, backgrounds, shapes
│   │   │   ├── values/
│   │   │   │   ├── colors.xml                  ← App color palette
│   │   │   │   ├── strings.xml                 ← All app text strings
│   │   │   │   ├── themes.xml                  ← Light/Dark theme definitions
│   │   │   │   └── dimens.xml                  ← Spacing & sizing constants
│   │   │   └── navigation/
│   │   │       └── nav_graph.xml               ← Navigation component graph
│   │   │
│   │   └── AndroidManifest.xml                 ← Permissions + component declarations
│   │
│   └── build.gradle.kts                        ← Dependencies
│
└── README.md
```

---

## SECTION 2 — ANDROID MANIFEST PERMISSIONS

The following permissions must be declared in `AndroidManifest.xml`:

```xml
<!-- Core App Permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- Floating Bubble Overlay -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- Accessibility Service (System-Wide Text Reading) -->
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />

<!-- Notifications (Daily Grammar Tips) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Prevent system from killing the Floating Bubble service -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

**Component Declarations:**
```xml
<!-- Floating Bubble Background Service -->
<service android:name=".service.FloatingBubbleService"
    android:foregroundServiceType="specialUse" />

<!-- Accessibility Service for system-wide text reading -->
<service android:name=".accessibility.GrammarAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

---

## SECTION 3 — NAVIGATION ARCHITECTURE

The app uses **Android Navigation Component** with the following flow:

```
┌──────────────────────────────────────────────────────────┐
│                    SPLASH / MAIN                         │
│              (Permission Setup + Onboarding)             │
└─────────────────────────┬────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────┐
│              BOTTOM NAVIGATION BAR                       │
│   [✏️ Editor]  [🤖 Chat]  [📋 Errors]  [📊 Dashboard]  │
└──────────────────────────────────────────────────────────┘
         │           │            │               │
         ▼           ▼            ▼               ▼
    Screen 1     Screen 2     Screen 3        Screen 4
  Smart Editor  AI Chatbot  Error Review    Dashboard
         │
         └──→ Screen 5: Settings (via toolbar icon)

[Floating Bubble] → Always visible system overlay (independent of navigation)
```

### Navigation Flow Details

| From | To | Trigger |
|---|---|---|
| Main/Splash | Smart Editor | App launch (first time shows onboarding) |
| Smart Editor | AI Chatbot | Tap FAB button OR tap "Learn Why" on error |
| Smart Editor | Error Review | Tap error count badge in toolbar |
| AI Chatbot | Smart Editor | Back button |
| Any Screen | Settings | Gear icon in top app bar |
| Floating Bubble | Mini Panel | Tap the bubble |
| Mini Panel | Smart Editor | Tap "Open in GrammarHelper" |

---

## SECTION 4 — SCREEN-BY-SCREEN DESIGN

---

### 📱 SCREEN 1 — Smart Editor (Home Screen)

**Purpose:** The main writing workspace. This is where users write text and receive real-time AI grammar analysis.

**Layout Blueprint (`activity_smart_editor.xml`):**
```
┌─────────────────────────────────────────┐
│  [☰] GrammarHelper    [📘][💼][😊] [⚙️] │  ← AppBar + Context Mode Chips + Settings
├─────────────────────────────────────────┤
│  Tone: 💼 Professional  ✅ Confident    │  ← Live Tone Indicator Banner
├─────────────────────────────────────────┤
│                                         │
│   Start typing your text here...        │
│                                         │
│   The quick brown fox jumps over the   │
│   ~~~~~~~~~~~                           │  ← 🔴 Red underline (grammar error)
│   lazy dog. Their are many reasons      │
│              ~~~~~                      │  ← 🔴 Red underline (contextual spelling)
│   why this sentence is used. It is     │
│   at this point in time considered     │
│   ~~~~~~~~~~~~~~~~~~~~~~~              │  ← 🔵 Blue underline (wordiness)
│   a classic example.                   │
│                                         │
│                                         │
│             [Tap error to fix ↑]        │
├─────────────────────────────────────────┤
│  Score: 72 | ❌ 3 Errors | 📝 47 Words │  ← Floating Stats Toolbar
├─────────────────────────────────────────┤
│  [Aa]  [Clear]  [Share]  [Copy]  [📋]  │  ← Action toolbar
└─────────────────────────────────────────┘
                                     [🤖]   ← AI Chatbot FAB (blue, bottom-right)
```

**Error Popup (appears when user taps an underline):**
```
┌────────────────────────────────────────┐
│ ❌ Grammar Error                        │
│ "Their" → should be "There"            │
│ (Contextual spelling mistake)          │
│                                        │
│   [✅ Fix]   [❌ Ignore]   [? Why]    │
└────────────────────────────────────────┘
```

**Key Components:**
| Component | Android Class | Purpose |
|---|---|---|
| Text Editor | `EditText` + `TextWatcher` | Captures user input, triggers analysis |
| Colored Underlines | `SpannableString` + `UnderlineSpan` | Displays red/blue error highlights |
| Error Popup | `PopupWindow` | Shows fix suggestion near the tapped error |
| Stats Toolbar | `LinearLayout` + `TextView` | Shows score, error count, word count |
| Context Mode | `ChipGroup` + `Chip` | Switches between Academic/Professional/Casual |
| AI Chatbot FAB | `FloatingActionButton` | Opens ChatbotActivity |

---

### 📱 SCREEN 2 — AI Chatbot

**Purpose:** The personal AI grammar tutor. Users can ask grammar questions, request rewrites, and take quizzes.

**Layout Blueprint (`activity_chatbot.xml`):**
```
┌─────────────────────────────────────────┐
│  [←] AI Grammar Tutor           [🗑️]   │  ← AppBar with back and clear history
├─────────────────────────────────────────┤
│ 📌 Context: "Their are many reasons..." │  ← Context bar showing discussed text
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────────┐   │
│  │ 🤖 Hello! I found 3 errors in   │   │  ← Bot message bubble (left aligned)
│  │ your text. Would you like me to │   │
│  │ explain them?                   │   │
│  └──────────────────────────────────┘   │
│                                         │
│          ┌─────────────────────────┐    │
│          │ Yes, explain the first  │    │  ← User message bubble (right aligned)
│          │ error please            │    │
│          └─────────────────────────┘    │
│                                         │
│  ┌──────────────────────────────────┐   │
│  │ 🤖 "Their" is a possessive      │   │
│  │ pronoun (e.g., "their house").  │   │
│  │ "There" refers to a place or   │   │
│  │ existence (e.g., "There is").   │   │
│  │ In your sentence, you need      │   │
│  │ "There are many reasons..."     │   │
│  └──────────────────────────────────┘   │
│                                         │
├─────────────────────────────────────────┤
│ [Explain This] [Rewrite] [Quiz Me] [🎯]│  ← Smart Quick Chips
├─────────────────────────────────────────┤
│ [🎤] Type a message...        [➤ Send] │  ← Input bar with voice option
└─────────────────────────────────────────┘
```

**Key Components:**
| Component | Android Class | Purpose |
|---|---|---|
| Chat Messages | `RecyclerView` + `ChatAdapter` | Displays conversation history |
| User Bubble | `item_chat_user.xml` | Blue bubble, right-aligned |
| Bot Bubble | `item_chat_bot.xml` | Grey/white bubble, left-aligned with bot icon |
| Quick Chips | `HorizontalScrollView` + `Chip` | Pre-built message shortcuts |
| Input Bar | `EditText` + `ImageButton` | User types question or request |
| Context Bar | `CardView` + `TextView` | Shows the text currently being discussed |

**AI Prompt Templates Used (from Prompt Builder):**

| User Action | Prompt Template |
|---|---|
| "Explain This" | `"Explain this grammar error in simple terms for a student: [error_type] in: '[original_text]'"` |
| "Rewrite" | `"Rewrite this text in 3 styles (Professional, Casual, Short): '[text]'"` |
| "Quiz Me" | `"Generate 3 multiple-choice grammar questions targeting the error type: [error_subtype]"` |
| Custom message | `"You are a helpful English grammar tutor. The user is asking: '[message]'. Context: '[text]'"` |

---

### 📱 SCREEN 3 — Error Review Panel

**Purpose:** A complete, categorized list of all grammar issues detected in the current text, allowing the user to review, fix, or dismiss each one.

**Layout Blueprint (`activity_error_review.xml`):**
```
┌─────────────────────────────────────────┐
│  [←] Error Review (3 Issues)    [Fix✅] │  ← AppBar + "Fix All" button
├─────────────────────────────────────────┤
│  [All 3] [✅ Correct 2] [💡 Clarity 1] │  ← Category filter tabs
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────────┐   │
│  │ ❌ CORRECTNESS                   │   │  ← Error card 1
│  │ "Their are many reasons"         │   │
│  │ ──────────────────────────────── │   │
│  │ Suggestion: "There are many..."  │   │
│  │ Type: Contextual Spelling        │   │
│  │       [✅ Fix]  [❌ Skip]  [? Why]│   │
│  └──────────────────────────────────┘   │
│                                         │
│  ┌──────────────────────────────────┐   │
│  │ ❌ CORRECTNESS                   │   │  ← Error card 2
│  │ "jumps over the lazy dog"        │   │
│  │ ──────────────────────────────── │   │
│  │ Suggestion: Check subject-verb   │   │
│  │ Type: Grammar — Tense Agreement  │   │
│  │       [✅ Fix]  [❌ Skip]  [? Why]│   │
│  └──────────────────────────────────┘   │
│                                         │
│  ┌──────────────────────────────────┐   │
│  │ 💡 CLARITY                       │   │  ← Error card 3
│  │ "at this point in time"          │   │
│  │ ──────────────────────────────── │   │
│  │ Suggestion: Replace with "now"   │   │
│  │ Type: Wordiness                  │   │
│  │       [✅ Fix]  [❌ Skip]  [? Why]│   │
│  └──────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

**Key Components:**
| Component | Android Class | Purpose |
|---|---|---|
| Category Filter | `TabLayout` | Switches between error categories |
| Error Cards | `RecyclerView` + `ErrorCardAdapter` | Scrollable list of error cards |
| Fix Button | `Button` (per card) | Applies the fix to the original text |
| Skip Button | `Button` (per card) | Dismisses the suggestion |
| "Why?" Button | `Button` → opens ChatbotActivity | Pre-loads explanation into chatbot |
| Fix All Button | `MenuItem` in AppBar | Fixes all errors at once |

---

### 📱 SCREEN 4 — Progress Dashboard

**Purpose:** Shows the user's grammar learning journey over time — charts, patterns, streaks, and badges.

**Layout Blueprint (`activity_dashboard.xml`):**
```
┌─────────────────────────────────────────┐
│  [←] My Progress              [📤 Share]│  ← AppBar
├─────────────────────────────────────────┤
│  🔥 7-Day Streak! Keep it up!           │  ← Streak Banner
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────────┐   │
│  │   Grammar Score Over Time 📈    │   │  ← Line Chart
│  │   100 |                    *     │   │
│  │    75 |          *    *  *       │   │
│  │    50 |   *   *                  │   │
│  │    25 |                          │   │
│  │       Mon Tue Wed Thu Fri Sat Sun│   │
│  └──────────────────────────────────┘   │
│                                         │
│  ┌──────────────────────────────────┐   │
│  │   Error Pattern Breakdown 🥧    │   │  ← Pie Chart
│  │                                  │   │
│  │   🔴 Correctness  45%            │   │
│  │   🔵 Clarity      30%            │   │
│  │   🟡 Tone         15%            │   │
│  │   🟢 Vocabulary   10%            │   │
│  └──────────────────────────────────┘   │
│                                         │
│  📋 Your Top 3 Mistakes                 │
│  1. Subject-Verb Agreement      12×     │
│  2. Contextual Spelling          8×     │
│  3. Passive Voice                5×     │
│                                         │
│  🏆 Your Badges                         │
│  [🥇 First Fix] [📅 7-Day] [💯 100 Fixes]│
│                                         │
└─────────────────────────────────────────┘
```

**Key Components:**
| Component | Android Library | Purpose |
|---|---|---|
| Line Chart | `MPAndroidChart` library | Grammar score over time |
| Pie Chart | `MPAndroidChart` library | Error type breakdown |
| Top Mistakes | `RecyclerView` or `ListView` | Ranked list from `error_log` DB |
| Badges Grid | `RecyclerView` + `BadgeAdapter` | Displays earned achievement badges |
| Streak Banner | `CardView` + `TextView` | Highlights current streak |
| Data Source | `ErrorLogDAO` + `SessionDAO` | Queries SQLite for analytics |

---

### 📱 SCREEN 5 — Settings

**Purpose:** Allows the user to fully customize the app behavior.

**Layout Blueprint (`activity_settings.xml`):**
```
┌─────────────────────────────────────────┐
│  [←] Settings                           │
├─────────────────────────────────────────┤
│                                         │
│  ── Writing Preferences ─────────────   │
│  Language Variant       [American ▾]    │
│  Default Context Mode   [Academic  ▾]   │
│                                         │
│  ── AI Preferences ──────────────────   │
│  Explanation Level      [Beginner  ▾]   │
│  Daily Grammar Tip      [ON  ◉]         │
│  Tip Notification Time  [08:00 AM  ▾]   │
│                                         │
│  ── Floating Bubble ─────────────────   │
│  Floating Bubble        [ON  ◉]         │
│  Bubble Size            [Medium    ▾]   │
│                                         │
│  ── Appearance ───────────────────────  │
│  Theme                  [System Default]│
│                                         │
│  ── Data & Privacy ───────────────────  │
│  [Clear Chat History]                   │
│  [Clear Error Log]                      │
│  [Export Progress Report as PDF]        │
│                                         │
│  ── About ────────────────────────────  │
│  Version: 1.0.0 | Group 20 | 2026      │
│                                         │
└─────────────────────────────────────────┘
```

---

## SECTION 5 — FLOATING BUBBLE ARCHITECTURE

**Files Involved:**
- `FloatingBubbleService.java`
- `GrammarAccessibilityService.java`
- `overlay_floating_bubble.xml`
- `overlay_suggestion_panel.xml`

**Lifecycle Flow:**
```
App Launched
     │
     ▼
User grants SYSTEM_ALERT_WINDOW permission
     │
     ▼
FloatingBubbleService starts as Foreground Service
     │
     ▼
WindowManager inflates overlay_floating_bubble.xml
(Small circular button pinned to screen edge)
     │
     ▼
GrammarAccessibilityService listens for TYPE_VIEW_TEXT_CHANGED events
     │
     ▼
User types in ANY app → Text extracted from AccessibilityEvent
     │
     ▼
500ms debounce timer → sends text to GeminiApiClient
     │
     ▼
AI returns error list (JSON)
     ├── 0 errors → Bubble stays 🟢
     └── 1+ errors → Bubble turns 🔴, badge shows count
              │
              ▼
         User taps bubble
              │
              ▼
WindowManager inflates overlay_suggestion_panel.xml
(Slides up from bottom of screen as an overlay)
              │
              ▼
User taps [Fix] → AccessibilityService injects corrected text
User taps [Fix All] → All suggestions applied at once
User taps [✕] → Panel dismissed, bubble returns to idle
```

**Bubble States:**
| State | Visual | Meaning |
|---|---|---|
| Idle (no text) | 🟢 Small circle | GrammarHelper is active, no issues |
| Analyzing | 🔵 Pulsing circle | AI is processing the text |
| Error Found | 🔴 Circle + badge number | Grammar issues detected |
| Minimized | Small dot at screen edge | User dragged it to minimize |

---

## SECTION 6 — AI ENGINE DESIGN

### API Communication Flow

```
SmartEditorActivity or FloatingBubbleService
                │
                │ text string
                ▼
        PromptBuilder.java
                │
                │ builds JSON prompt payload
                ▼
        GeminiApiClient.java
                │
                │ HTTP POST → Gemini API
                ▼
        AI Response (JSON string)
                │
                ▼
     GrammarResponseParser.java
                │
       ┌────────┴────────┐
       ▼                 ▼
List<GrammarError>   ToneResult
       │                 │
       ▼                 ▼
TextHighlighter    Tone Emoji Indicator
SpannableString    (Top toolbar)
       │
       ▼
ErrorLogDAO → Save to SQLite
```

### Prompt A — Grammar Analysis (Main Prompt)
```
System: You are a professional grammar analysis engine.
Analyze the following text and return ONLY a valid JSON array.
Each object in the array must have:
- "error_type": "Correctness" | "Clarity" | "Engagement" | "Tone"
- "error_subtype": specific rule (e.g., "Subject-Verb Agreement")
- "original": the exact problematic text
- "suggestion": the corrected version
- "explanation": one simple sentence explaining why (max 20 words)
- "position_start": character index of error start
- "position_end": character index of error end

Text to analyze: "[USER_TEXT]"
Context mode: [Academic / Professional / Casual]
```

### Prompt B — Explain Error (Chatbot)
```
System: You are a friendly English grammar tutor for students.
Explain the following grammar error clearly and simply.
Use a real-life example. Keep it under 80 words.
Max difficulty: [Beginner / Intermediate / Expert]

Error type: [error_subtype]
Original text: "[original]"
Suggested fix: "[suggestion]"
```

### Prompt C — Rewrite in 3 Styles
```
System: Rewrite the following text in exactly 3 versions.
Label each version clearly as:
PROFESSIONAL: [rewritten text]
CASUAL: [rewritten text]
SHORT: [rewritten text — max 60% of original length]

Text: "[USER_TEXT]"
```

### Prompt D — Generate Quiz
```
System: Generate exactly 3 multiple-choice grammar questions
targeting the error type: "[error_subtype]".
Format each question as:
Q: [question]
A) [option]  B) [option]  C) [option]  D) [option]
Answer: [correct letter]
Explanation: [one sentence]
```

### Prompt E — Tone Detection
```
System: Analyze the tone of this text.
Return ONLY a JSON object:
{
  "tone": "Formal" | "Friendly" | "Assertive" | "Blunt" | "Casual" | "Optimistic",
  "formality": "High" | "Medium" | "Low",
  "confidence": "High" | "Medium" | "Low",
  "suggestion": "[one-line advice if tone needs improvement]"
}
Text: "[USER_TEXT]"
```

---

## SECTION 7 — DATABASE DESIGN (SQLite)

### `DatabaseHelper.java` — Creates All Tables
```java
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "grammar_helper.db";
    private static final int    DB_VERSION = 1;

    // Called once on app first launch
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SESSIONS_TABLE);
        db.execSQL(CREATE_ERROR_LOG_TABLE);
        db.execSQL(CREATE_CHAT_HISTORY_TABLE);
    }

    // Table 1
    String CREATE_SESSIONS_TABLE =
        "CREATE TABLE user_sessions (" +
        "session_id    INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "text_content  TEXT NOT NULL, " +
        "grammar_score INTEGER, " +
        "clarity_score INTEGER, " +
        "context_mode  TEXT, " +
        "tone_detected TEXT, " +
        "word_count    INTEGER, " +
        "timestamp     TEXT)";

    // Table 2
    String CREATE_ERROR_LOG_TABLE =
        "CREATE TABLE error_log (" +
        "error_id      INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "session_id    INTEGER, " +
        "error_type    TEXT, " +
        "error_subtype TEXT, " +
        "original_text TEXT, " +
        "suggestion    TEXT, " +
        "was_accepted  INTEGER)";    // 1 = fixed, 0 = dismissed

    // Table 3
    String CREATE_CHAT_HISTORY_TABLE =
        "CREATE TABLE chat_history (" +
        "chat_id      INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "user_message TEXT, " +
        "bot_response TEXT, " +
        "context_text TEXT, " +
        "timestamp    TEXT)";
}
```

### Dashboard Queries (from `ErrorLogDAO.java`)
```sql
-- Get top 5 most frequent error subtypes
SELECT error_subtype, COUNT(*) as count
FROM error_log
GROUP BY error_subtype
ORDER BY count DESC
LIMIT 5;

-- Get error distribution by type (for Pie Chart)
SELECT error_type, COUNT(*) as count
FROM error_log
GROUP BY error_type;

-- Get grammar score trend (for Line Chart)
SELECT grammar_score, timestamp
FROM user_sessions
ORDER BY timestamp ASC
LIMIT 30;
```

---

## SECTION 8 — COLOR PALETTE & DESIGN SYSTEM

### Color Tokens
```xml
<!-- colors.xml -->
<color name="primary">         #2563EB </color>  <!-- GrammarHelper Blue -->
<color name="primary_dark">    #1D4ED8 </color>  <!-- Darker Blue -->
<color name="accent">          #7C3AED </color>  <!-- Purple (AI/Chatbot) -->
<color name="error_red">       #DC2626 </color>  <!-- Grammar Error Underline -->
<color name="clarity_blue">    #2563EB </color>  <!-- Clarity Issue Underline -->
<color name="tone_yellow">     #D97706 </color>  <!-- Tone Issue Underline -->
<color name="success_green">   #16A34A </color>  <!-- Bubble OK state -->
<color name="surface">         #F8FAFC </color>  <!-- Card backgrounds -->
<color name="background">      #FFFFFF </color>  <!-- Light mode bg -->
<color name="text_primary">    #0F172A </color>  <!-- Main text -->
<color name="text_secondary">  #64748B </color>  <!-- Subtext, labels -->

<!-- Dark Mode -->
<color name="bg_dark">         #0F172A </color>
<color name="surface_dark">    #1E293B </color>
<color name="text_dark">       #F1F5F9 </color>
```

### Error Color Coding System
| Error Category | Underline Color | Hex |
|---|---|---|
| Correctness (Grammar/Spelling) | 🔴 Red | `#DC2626` |
| Clarity (Wordiness/Passive) | 🔵 Blue | `#2563EB` |
| Engagement (Vocabulary) | 🟢 Green | `#16A34A` |
| Tone & Delivery | 🟡 Yellow/Amber | `#D97706` |

---

## SECTION 9 — DEPENDENCIES ([build.gradle.kts](file:///c:/Users/asus/AndroidStudioProjects/GrammarHelper/build.gradle.kts))

```kotlin
dependencies {
    // Android Core
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // Charts (for Dashboard)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // HTTP Client (for Gemini API calls)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON Parsing (for AI responses)
    implementation("com.google.code.gson:gson:2.11.0")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.3")
}
```

---

## SECTION 10 — PHASED DEVELOPMENT ROADMAP

```
PHASE 1 — Foundation (Week 1–2)
├── Set up Android project structure (all packages)
├── Implement DatabaseHelper + all 3 DAOs
├── Build SmartEditorActivity with basic EditText
├── Implement TextHighlighter with SpannableString
└── Connect to Gemini API (Prompt A – Grammar Analysis)

PHASE 2 — Core Features (Week 3–4)
├── Display colored underlines from AI response
├── Build ErrorPopup on tap (One-Tap Fix)
├── Implement ErrorReviewActivity + ErrorCardAdapter
├── Build ChatbotActivity + ChatAdapter
└── Connect Chatbot to Gemini API (Prompts B, C, D, E)

PHASE 3 — System-Wide Floating Bubble (Week 5–6)
├── Implement FloatingBubbleService with WindowManager
├── Set up GrammarAccessibilityService
├── Build overlay_floating_bubble.xml and overlay_suggestion_panel.xml
└── Test Floating Bubble across WhatsApp, Gmail, Chrome

PHASE 4 — Dashboard & Analytics (Week 7)
├── Implement DashboardActivity
├── Integrate MPAndroidChart (Line + Pie charts)
├── Write SQL queries for top mistakes and score trends
└── Implement Achievement Badges system

PHASE 5 — Polish & Settings (Week 8)
├── Implement SettingsActivity
├── Add Light/Dark theme toggle
├── Implement Daily Grammar Tip push notification
├── Export Progress Report as PDF
└── Final testing, bug fixes, and UI polish
```

---

*End of Project Architecture & Screen Design Document*
*Group 20 | GrammarHelper | March 2026*
