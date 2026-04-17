# GrammarHelper
## The Ultimate Mobile App Concept Document
### *A Better Grammarly — Mobile-First. AI-Powered. Educationally Driven.*

> **Group 20 | Master Concept Reference**
> *Finalized: March 2026*

---

## PART 1 — APP IDENTITY

### 1.1 App Name
**GrammarHelper**

### 1.2 Tagline
> *"Your pocket-sized English writing coach — anywhere you type."*

### 1.3 Platform
- **Primary:** Android Mobile Application (Java)
- **Future Expansion:** iOS (Phase 2)

### 1.4 Vision Statement
GrammarHelper is a **mobile-first, AI-integrated grammar assistant** that goes beyond what Grammarly's desktop version offers. It does not just correct your English — it **teaches you why** through a built-in AI chatbot tutor, adapts to your writing context, and follows you across **every app on your phone** through a smart Floating Bubble — making it the first truly system-wide grammar assistant for mobile.

### 1.5 Mission Statement
To make high-quality English writing assistance **free, accessible, and educational** for students, professionals, and non-native English speakers on the device they use most — their mobile phone.

---

## PART 2 — THE PROBLEM & OPPORTUNITY

### 2.1 The Core Problem
| The Gap | Details |
|---|---|
| **Platform Gap** | Grammarly is designed for Desktop and Browser. There is no full-featured Grammarly for mobile. |
| **Learning Gap** | Existing tools fix mistakes but never explain *why* they are wrong. Users keep repeating the same errors. |
| **Access Gap** | Students in developing countries are more likely to own a phone than a laptop — yet grammar tools ignore them. |
| **Context Gap** | Mobile users write in many different contexts (academic essays, WhatsApp chats, professional emails) and need a tool that adapts. |

### 2.2 The Opportunity
By combining **full Grammarly-grade grammar checking**, **an AI chatbot tutor**, and a **system-wide Floating Bubble**, GrammarHelper becomes the **first truly complete mobile grammar assistant** — addressing all four gaps above.

### 2.3 Target Users
| User Group | Use Case |
|---|---|
| **University Students** | Writing essays, assignment submissions, emails to lecturers |
| **Non-Native English Speakers** | Building confidence in everyday English writing |
| **Working Professionals** | Drafting work emails, reports, and messages on the go |
| **Secondary School Students** | Improving grammar skills in a learning-friendly environment |

---

## PART 3 — THE 6 CORE FEATURE PILLARS

---

### 🟥 PILLAR 1 — Correctness *(The Foundation)*

The first and most essential layer of the app — catching and fixing what is grammatically wrong.

| Sub-Feature | What It Does |
|---|---|
| **Grammar Engine** | Detects subject-verb disagreement, wrong tenses, dangling modifiers, incomplete sentences |
| **Contextual Spelling** | Catches correctly-spelled but wrongly-used words ("their" vs "there", "affect" vs "effect") |
| **Advanced Punctuation** | Flags comma splices, run-on sentences, missing apostrophes, incorrect semicolons |
| **Article & Preposition Check** | Catches "a" vs "an", wrong prepositions (common for non-native speakers) |
| **Language Variant Support** | Supports American English and British English spelling conventions |

**📱 Mobile Implementation:**
- Real-time **red underlines** appear as the user types (with a 500ms smart delay to avoid flickering)
- Tapping an underlined word opens a **"One-Tap Fix" popup bubble** with the correction and a short label like *"Wrong tense"*
- User can choose **"Fix"** or **"Ignore"** directly from the popup — no screen switching needed

---

### 🟦 PILLAR 2 — Clarity & Conciseness

Making writing easier to understand — cutting out the noise to get to the point.

| Sub-Feature | What It Does |
|---|---|
| **Wordiness Detector** | Flags bloated phrases: "at this point in time" → suggests "now" |
| **Sentence Rewriter** | Offers a shorter, cleaner alternative for any sentence that is too long or complex |
| **Passive Voice Detector** | Highlights passive voice constructions and suggests active rewrites |
| **Clarity Score (0–100)** | A live numerical score showing how easy the overall text is to read |
| **Readability Grade Level** | Tells the user if the text reads at Grade 5, Grade 8 or Grade 12 level |

**📱 Mobile Implementation:**
- **Blue highlights** on wordy or unclear phrases
- A persistent **Clarity Score gauge** on the floating toolbar above the keyboard — updates live as the user writes
- One tap on a blue highlight shows the suggested rewrite

---

### 🟩 PILLAR 3 — Engagement & Vocabulary

Keeping the reader engaged with stronger, more varied, and more precise word choices.

| Sub-Feature | What It Does |
|---|---|
| **Vocabulary Enhancer** | Suggests more vivid or professional synonyms for weak or overused words |
| **Word Repetition Detector** | Flags when the same word is used too many times in proximity and suggests alternatives |
| **Vivid Language Suggestions** | Replaces generic words ("very good", "bad") with expressive alternatives ("exceptional", "detrimental") |
| **Vocabulary Level Indicator** | Tells the user whether their vocabulary level is Basic, Intermediate, or Advanced |

**📱 Mobile Implementation:**
- **Long-press any word** → a "Word Picker" chip appears above the selection with 3–5 AI-ranked synonym options
- Tapping any synonym replaces the word instantly with a subtle animation

---

### 🟨 PILLAR 4 — Tone & Delivery Intelligence

Understanding *how* the text sounds, not just *what* it says.

| Sub-Feature | What It Does |
|---|---|
| **Live Tone Detector** | Classifies tone in real-time: Formal, Friendly, Assertive, Optimistic, Blunt, Casual |
| **Context Mode Switcher** | User selects Academic 📘 / Professional 💼 / Casual 😊 before writing; AI adjusts feedback accordingly |
| **Confidence Booster** | Removes hedging language ("I think", "maybe", "sort of") to make writing sound more direct |
| **Politeness Checker** | Flags text that may unintentionally sound rude, cold, or passive-aggressive |
| **Formality Adjuster** | Can rewrite text up or down the formality scale on demand |
| **Audience Awareness** | In Academic mode, the AI enforces no contractions, no slang, and formal vocabulary |

**📱 Mobile Implementation:**
- A live **Tone Emoji Indicator** sits in the top toolbar (📘 Academic / 💼 Professional / 😊 Casual / ⚠️ Blunt)
- The emoji updates automatically every few seconds as the user types
- Tapping the emoji shows a tooltip: *"Your writing sounds formal and confident"*

---

### 🟪 PILLAR 5 — AI Chatbot Assistant *(The Core Differentiator)*

A personal AI English tutor built directly into the app. This is what Grammarly fundamentally does NOT have.

| Sub-Feature | What It Does |
|---|---|
| **"Explain Why?" Mode** | Tap any flagged error → tap "Explain" → chatbot gives a beginner-friendly explanation of the grammar rule behind it |
| **Natural Language Commands** | Type freely: *"Make this sound more professional"* or *"Is my tone ok for an email to a professor?"* |
| **3-Style AI Rewriter** | Select any text → the AI rewrites it in 3 distinct styles: **Professional**, **Casual**, and **Short** |
| **Brainstorming Partner** | *"Help me write an opening sentence for an essay on climate change"* |
| **Personalized Grammar Quizzes** | The chatbot generates quizzes targeting the user's most frequent mistake types from their history |
| **Grammar Tip of the Day** | The bot sends a daily AI-generated grammar lesson based on actual patterns in the user's writing history |
| **Full Explanation Mode** | Provides detailed grammar rule breakdowns (not just "this is wrong", but "here is the rule and 2 examples") |
| **Persistent Chat History** | All chatbot conversations are saved locally in SQLite — the user can scroll back and review past lessons |

**📱 Mobile Implementation:**
- A persistent **blue FAB (Floating Action Button)** at the bottom-right of the Smart Editor
- Tapping opens the **Chatbot Screen** with a clean chat bubble layout
- **Smart Quick Chips** appear above the input bar: "Explain This" | "Rewrite This" | "Quiz Me" | "Check Tone"
- The chatbot uses a **Beginner / Intermediate / Expert** response mode (set in Settings) so explanations are always at the right level

---

### 🟧 PILLAR 6 — Floating Bubble *(System-Wide Grammar Check — Mobile Ctrl+G)*

The biggest mobile innovation — taking the spirit of Grammarly's **Ctrl+G desktop shortcut** and supercharging it for Android.

#### The Concept
On Grammarly Desktop, Ctrl+G opens a suggestion panel that works across all apps. On GrammarHelper mobile, the **Floating Bubble** does the same — but it is always visible, always active, and works in every single app on the phone.

#### How It Works — Step by Step
```
1. User opens ANY app (WhatsApp, Gmail, Google Docs, Instagram, Notes...)
2. GrammarHelper Floating Bubble (🟢) sits on screen as a small overlay
3. User starts typing in that app
4. GrammarHelper reads the text via AccessibilityService (with user permission)
5. AI analyzes the text in the background (500ms debounce delay)
6. If errors are found → Bubble turns 🔴 with a badge showing error count (e.g., 🔴 3)
7. User taps the bubble
8. A mini overlay panel slides up showing suggestion cards:
   ┌─────────────────────────────────────────────┐
   │ ❌ "their" should be "there"     [Fix] [Skip]│
   │ ❌ Missing comma after "however" [Fix] [Skip]│
   │ ⚠️  Tone sounds blunt            [Fix] [Skip]│
   │             [Fix All]  [Dismiss All]         │
   └─────────────────────────────────────────────┘
9. User taps "Fix All" → corrections applied instantly in the original app
10. Bubble turns back to 🟢
```

| Sub-Feature | Details |
|---|---|
| **Always-On Bubble** | Floating 🟢/🔴 indicator on screen across ALL apps — can be repositioned by dragging |
| **Mini Overlay Panel** | Quick suggestion cards appear on bubble tap — no need to open GrammarHelper main app |
| **One-Tap Fix / Fix All** | Accept or dismiss any suggestion directly from the overlay |
| **Error Count Badge** | The bubble shows a number badge (e.g., 🔴 3) indicating how many issues were found |
| **Smart Debounce Delay** | Analysis triggers only after 500ms of typing pause, preventing lag and battery drain |
| **Bubble Toggle** | Can be turned on/off from within the app Settings or by long-pressing the bubble |

**📱 Tech Behind It:**
- `WindowManager` — renders the bubble as a system-level overlay
- `Foreground Service` — keeps the analysis running without the app being open
- `AccessibilityService` — reads text from any active text field in any app (requires user permission, clearly explained on first use)

> ✅ **This makes GrammarHelper the first Android app to offer true system-wide, real-time AI grammar checking — across every app — without needing to copy-paste text.**

---

## PART 4 — APP SCREENS & FULL UX FLOW

### Screen 1 — 🏠 Smart Editor (Home)
The main writing workspace.

| Element | Description |
|---|---|
| **Top Bar** | App logo + Context Mode Switcher chips (📘 Academic / 💼 Professional / 😊 Casual) |
| **Tone Emoji Indicator** | Live emoji displaying current detected tone, updates as user types |
| **Writing Area** | Full-screen text editor with live colored underlines (🔴 Grammar, 🔵 Clarity, 🟡 Tone) |
| **Floating Toolbar** | Displays: Clarity Score | Error Count | Word Count | Character Count |
| **Error Highlight Popups** | Tap any underline → popup with suggested fix + "Learn Why" option |
| **AI Chatbot FAB** | Persistent blue button (bottom-right) to open the chatbot screen |

---

### Screen 2 — 🤖 AI Chatbot
The AI tutor conversation screen.

| Element | Description |
|---|---|
| **Chat Bubbles** | User messages on the right (blue), AI responses on the left (white/grey) |
| **Context Bar** | Shows the sentence/paragraph currently being discussed |
| **Smart Quick Chips** | Tap-to-send common actions: "Explain This" / "Rewrite This" / "Quiz Me" / "Check Tone" |
| **Input Bar** | Typing area with send button and voice input option |
| **Chat History** | Scrollable — all past conversations are saved and accessible |

---

### Screen 3 — 📋 Error Review Panel
A full list of all detected issues, sorted by category.

| Element | Description |
|---|---|
| **Category Tabs** | Filter by: All / ✅ Correctness / 💡 Clarity / 🎨 Vocabulary / 🎭 Tone |
| **Error Cards** | Each card shows: the original text (highlighted), the issue type, and the suggested fix |
| **Fix / Dismiss Controls** | One-tap buttons per card |
| **"Learn Why" Button** | Opens the AI chatbot with a pre-loaded explanation for that specific error |
| **Bulk Actions** | "Fix All Correctness Errors" or "Dismiss All Tone Warnings" |

---

### Screen 4 — 📊 Progress Dashboard
Personalized learning analytics — the user's grammar improvement over time.

| Element | Description |
|---|---|
| **Grammar Score Over Time** | Line chart showing session-by-session score improvement |
| **Error Pattern Breakdown** | Pie chart categorizing the user's most common error types |
| **Top 5 Mistakes Table** | A ranked list of the user's most frequent mistakes with counts |
| **Writing Activity Calendar** | A GitHub-style heatmap showing daily writing sessions |
| **Streak Counter** | Consecutive days of app usage, with milestone badges (e.g., 🔥 7-Day Streak) |
| **Achievement Badges** | Earned for milestones: "First Fix", "100 Errors Corrected", "7-Day Streak", etc. |

---

### Screen 5 — ⚙️ Settings
Full user customization.

| Setting | Options |
|---|---|
| **Language Variant** | American English / British English |
| **Default Context Mode** | Academic / Professional / Casual / Auto-Detect |
| **Floating Bubble** | Toggle ON/OFF, Bubble Size (Small / Medium) |
| **AI Explanation Level** | Beginner / Intermediate / Expert |
| **Daily Grammar Tip** | Toggle ON/OFF, Notification Time |
| **Theme** | Light / Dark / System Default |
| **Data & Privacy** | Clear chat history, Clear error log, Export report as PDF |

---

## PART 5 — DATA ARCHITECTURE (SQLite)

Three tables power all the app's intelligence and personalization.

### Table 1: `user_sessions`
Stores each writing session with its analysis results.
```sql
CREATE TABLE user_sessions (
    session_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    text_content  TEXT NOT NULL,
    grammar_score INTEGER,      -- Overall score 0-100
    clarity_score INTEGER,      -- Clarity score 0-100
    context_mode  TEXT,         -- "Academic" / "Professional" / "Casual"
    tone_detected TEXT,         -- e.g. "Formal", "Friendly", "Blunt"
    word_count    INTEGER,
    timestamp     TEXT
);
```

### Table 2: `error_log`
Tracks every single detected error for Dashboard pattern analysis.
```sql
CREATE TABLE error_log (
    error_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id    INTEGER REFERENCES user_sessions(session_id),
    error_type    TEXT,        -- "Correctness" / "Clarity" / "Engagement" / "Tone"
    error_subtype TEXT,        -- e.g. "Subject-Verb Agreement", "Passive Voice"
    original_text TEXT,
    suggestion    TEXT,
    was_accepted  INTEGER      -- 1 = fixed by user, 0 = dismissed
);
```

### Table 3: `chat_history`
Stores all AI chatbot conversations for review and continuity.
```sql
CREATE TABLE chat_history (
    chat_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_message TEXT,
    bot_response TEXT,
    context_text TEXT,         -- The sentence/paragraph being discussed
    timestamp    TEXT
);
```

---

## PART 6 — TECHNICAL ARCHITECTURE

```
┌──────────────────────────────────────────────────────────────┐
│                    ANDROID APP (Java)                        │
│                                                              │
│  ┌─────────────────┐   ┌──────────────────┐                 │
│  │ Smart Editor    │   │ Floating Bubble   │                 │
│  │ (TextWatcher)   │   │ (Foreground       │                 │
│  │ SpannableString │   │  Service +        │                 │
│  │ for underlines  │   │  WindowManager +  │                 │
│  └────────┬────────┘   │  Accessibility    │                 │
│           │            │  Service)         │                 │
│           │            └────────┬──────────┘                │
│           └──────────┬──────────┘                            │
│                      ↓                                       │
│             JSON Request Builder                             │
│                      │                                       │
└──────────────────────┼───────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│              AI ENGINE (Gemini API / OpenAI API)             │
│                                                              │
│  Prompt A → "Analyze text, return JSON of errors"           │
│  Prompt B → "Explain this error simply for a student"       │
│  Prompt C → "Rewrite in Professional / Casual / Short"      │
│  Prompt D → "Generate a quiz targeting [error_subtype]"     │
│  Prompt E → "Detect tone and formality level"               │
└──────────────────────┼───────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│                 JSON RESPONSE PARSER                         │
│                                                              │
│  → Maps error categories to underline colors (UI)           │
│  → Populates Error Review Panel cards                        │
│  → Feeds chatbot response into RecyclerView                  │
│  → Updates Clarity Score and Tone Emoji in toolbar           │
│  → Saves session + errors to SQLite                          │
│  → Triggers Floating Bubble color change (🟢/🔴)            │
└──────────────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│               SQLite LOCAL DATABASE                          │
│  user_sessions | error_log | chat_history                    │
│  → Feeds Progress Dashboard charts and tables                │
│  → Enables offline review of past sessions                   │
└──────────────────────────────────────────────────────────────┘
```

---

## PART 7 — COMPETITIVE COMPARISON

| Feature | Grammarly (Desktop) | GrammarHelper (Mobile) |
|---|---|---|
| **Platform** | Desktop / Browser | ✅ Android Mobile |
| **Grammar Check** | ✅ Yes | ✅ Yes |
| **Contextual Spelling** | ✅ Yes | ✅ Yes |
| **Clarity & Conciseness** | ✅ Yes | ✅ Yes |
| **Tone Detection** | ✅ Yes | ✅ Yes |
| **Context Modes** | Basic Goals | ✅ Smart Academic/Professional/Casual |
| **System-Wide Check (Ctrl+G)** | ✅ Desktop only | ✅ Floating Bubble — ANY app on phone |
| **AI Chatbot Tutor** | ❌ No | ✅ Yes |
| **"Explain Why" per Error** | ❌ No | ✅ Yes |
| **3-Style AI Rewriting** | ❌ No | ✅ Yes |
| **Personalized Error Dashboard** | ❌ Limited | ✅ Full Charts & Patterns |
| **Grammar Quizzes** | ❌ No | ✅ Yes |
| **Daily AI Grammar Tips** | ❌ No | ✅ Yes |
| **Offline SQLite History** | ❌ No | ✅ Yes |
| **Achievement Badges** | ❌ No | ✅ Yes |
| **Price** | Freemium (Paid premium) | ✅ Free |

---

## PART 8 — SDG ALIGNMENT (Goal 4: Quality Education)

| SDG 4 Target | How GrammarHelper Addresses It |
|---|---|
| **4.1 Free & Equitable Education** | The app is free and available to anyone with an Android phone |
| **4.4 Skills for Employment** | Improves professional writing — a core employability skill |
| **4.6 Literacy Improvement** | Directly improves English writing literacy through AI feedback |
| **4.7 Education for Sustainable Development** | Personalized learning fosters lifelong writing improvement |

---

## PART 9 — WHAT MAKES GRAMMARHELPER UNIQUE (One Paragraph Summary)

GrammarHelper is not a copycat of Grammarly. It takes the **best grammar functionalities** from the Grammarly desktop experience — Correctness, Clarity, Engagement, and Tone Detection — and rebuilds them natively for Android mobile users. It then adds **three brand-new layers** that no competitor currently offers: an **AI Chatbot Tutor** that explains every error in plain language, a **Personalized Progress Dashboard** that tracks error patterns over time and rewards improvement, and a **Floating Bubble** that brings system-wide, real-time grammar checking to every app on the phone — the true mobile evolution of Grammarly's Ctrl+G shortcut. GrammarHelper is not just a writing tool. It is a **pocket-sized English writing coach**.

---

*End of Master Concept Document — Group 20 | GrammarHelper | March 2026*
