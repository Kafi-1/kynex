Kynex AI — Development Guide

You are working on a native Android AI chat application called Kynex AI.

Before writing or changing major code, understand the complete project requirements and follow this development guide.

---

1. MOST IMPORTANT — UI/UX REFERENCE

There is an existing HTML file in the project named:

"demo.html"

This file is the primary UI/UX reference for the Android application.

The Android app must reproduce the same visual design and layout as "demo.html".

Inspect "demo.html" carefully before implementing the Compose UI.

Match as closely as practical:

- Overall layout
- Header
- Navigation
- Sidebar
- Chat area
- Message bubbles
- Input area
- Buttons
- Icons
- Model selector
- Cards
- Spacing
- Padding
- Border radius
- Typography hierarchy
- Font sizing
- Background
- Theme
- Dark/light appearance
- Empty states
- Loading states
- Settings layout
- Profile layout
- Chat history layout
- Saved chats layout
- Responsive behavior translated appropriately to Android

Do NOT create a completely different Android design.

Do NOT replace the "demo.html" design with a generic AI-chat UI.

The goal is:

demo.html → native Kotlin/Jetpack Compose equivalent

The HTML implementation itself does NOT need to be used inside a WebView.

Build the interface natively using Jetpack Compose while visually following "demo.html".

---

2. FIRST TASK — INSPECT THE PROJECT

Before coding:

1. Inspect the entire project structure.
2. Find "demo.html".
3. Read and understand its HTML structure.
4. Inspect its CSS.
5. Inspect its JavaScript if present.
6. Identify all screens/components represented by the demo.
7. Identify assets, icons, images and fonts.
8. Identify reusable UI components.
9. Identify existing Kotlin/Android code.
10. Determine what can be reused and what needs to be implemented.

Do not delete existing working code without a reason.

Do not blindly overwrite files.

---

3. APPLICATION STACK

Use:

- Kotlin
- Jetpack Compose
- Android SDK
- Kotlin Coroutines
- Flow where useful
- Firebase Authentication
- Cloud Firestore
- OkHttp/Retrofit or another stable HTTP client
- Navigation Compose
- Material 3 only where it helps; customize it to match "demo.html"

Do NOT use:

- React
- Flutter
- WebView as the main UI
- Node.js backend
- unnecessary frameworks

---

4. APPLICATION FEATURES

Kynex AI will be a multi-model AI chat application.

Main features:

Authentication

- Login
- Sign Up
- Continue with Google
- Continue with GitHub
- Logout
- Persistent authentication session

AI Chat

- New chat
- Send message
- Receive AI response
- Streaming response if supported
- Model selection
- Conversation context
- Markdown
- Code blocks
- Copy message
- Copy code
- Regenerate response
- Retry failed request
- Loading state
- Error state

History

- Save conversations
- View previous conversations
- Search chats
- Rename chats
- Delete chats
- Continue old conversations

Saved Chats

- Save/bookmark conversations
- View saved conversations
- Remove from saved

Settings

- Selected model
- Theme preference
- Account information
- Logout
- About

---

5. AI MODEL SYSTEM

The application will support multiple AI models through AgentRouter.

Initial model list:

- GPT-5.6-Sol
- Claude Opus 4.8
- Claude Opus 5
- DeepSeek V4 Flash
- GLM 5.3

IMPORTANT:

The user-facing model name and actual API model ID must be separate.

Example:

data class AiModel(
    val id: String,
    val displayName: String,
    val provider: String
)

Never assume that:

"displayName == API model ID"

Keep all model configuration centralized so models can easily be changed later.

---

6. AGENTROUTER API

The AI API provider is:

AgentRouter

Base URL:

"https://agentrouter.org/"

Implement the API layer separately from the UI.

Architecture:

Compose UI
     ↓
ViewModel
     ↓
Chat Repository
     ↓
AI Service
     ↓
AgentRouter
     ↓
Selected AI Model

The UI must never directly perform HTTP requests.

The AI service should handle:

- Base URL
- API authentication
- Model ID
- Messages
- System prompt
- Streaming
- Non-streaming fallback
- Timeout
- API errors
- Network errors
- Retry

Keep API configuration in one centralized location.

Do not duplicate the API key throughout the project.

The owner understands that API credentials embedded in an APK cannot be considered fully secret.

---

7. FIREBASE ARCHITECTURE

Firebase will be used for authentication and user data.

Use:

Firebase Authentication

Providers:

- Email/password
- Google
- GitHub

Cloud Firestore

Recommended structure:

users
 └── {uid}
      ├── profile
      │
      ├── settings
      │
      └── chats
           └── {chatId}
                ├── title
                ├── modelId
                ├── createdAt
                ├── updatedAt
                ├── isSaved
                ├── messageCount
                │
                └── messages
                     └── {messageId}
                          ├── role
                          ├── content
                          ├── modelId
                          ├── createdAt
                          └── isError

Use Firestore as the permanent source of truth for authenticated chat history.

---

8. FIRESTORE SECURITY

Security rules are mandatory.

Users must only be able to access their own data.

Conceptually:

User A
  ↓
users/A/*
  ✓ allowed

User A
  ↓
users/B/*
  ✗ denied

Never use insecure rules such as:

allow read, write: if true;

---

9. CHAT FLOW

When the user sends a message:

User enters message
        ↓
Validate input
        ↓
Create/save user message
        ↓
Send request to AgentRouter
        ↓
Selected model generates response
        ↓
Display response
        ↓
Save AI response
        ↓
Update chat metadata

If the API fails:

API error
   ↓
Show friendly error
   ↓
Allow Retry

Do not crash the application.

Do not save duplicate messages during retry.

---

10. CHAT HISTORY

Each conversation should have:

- Unique chat ID
- Title
- Selected model
- Created time
- Updated time
- Saved status
- Messages

Automatically generate a reasonable initial title from the first user message, but allow manual rename.

History should be sorted by latest activity.

Do not load thousands of messages at once.

Use pagination/limited loading where appropriate.

---

11. SAVED CHAT

A conversation can be marked:

isSaved = true

Saved conversations should appear in the Saved section.

The saved state must persist through Firestore.

---

12. LOCAL STORAGE

Use local storage/Preferences/DataStore for lightweight preferences such as:

- Theme
- Last selected model
- UI preferences

Do NOT use local storage as the primary permanent chat database.

Firestore handles permanent authenticated user history.

---

13. ANDROID ARCHITECTURE

Use a clean but practical architecture.

presentation/
    screens/
    components/
    viewmodels/

data/
    auth/
    firestore/
    network/
    repository/

domain/
    model/
    repository/

Do not over-engineer.

The code should remain understandable for a developer maintaining the project later.

---

14. REQUIRED COMPOSE SCREENS

Implement screens based on the actual "demo.html" design.

At minimum:

Splash
Login
Sign Up
Home
Chat
Chat History
Saved Chats
Model Selection
Profile
Settings
About

If "demo.html" contains additional sections/screens, implement those too.

---

15. RESPONSIVE ANDROID BEHAVIOR

Translate the responsive behavior of "demo.html" into native Android behavior.

For example:

Desktop-style sidebar from the HTML:

→ Android navigation drawer / adaptive navigation.

HTML chat input:

→ Native Compose input bar.

HTML modal:

→ Compose Dialog / ModalBottomSheet as appropriate.

HTML dropdown:

→ Compose dropdown/menu.

Do not simply copy HTML dimensions.

Adapt them naturally to Android while preserving the same visual identity.

---

16. ERROR HANDLING

Handle:

- No internet
- API timeout
- API error
- Invalid response
- Authentication failure
- Firebase permission error
- Firestore failure
- Empty response
- Rate limit
- Server error

Every failure should produce a user-friendly UI state.

Never allow an exception from a network request to crash the app.

---

17. PERFORMANCE

The application should run reasonably well on low-end Android devices.

Avoid:

- unnecessary recompositions
- huge in-memory chat histories
- unnecessary Firestore reads
- blocking main-thread operations
- heavy animations
- unnecessary image processing

Use lazy lists for chat/history lists.

Use Coroutines for asynchronous work.

---

18. DEVELOPMENT PHASES

Do NOT attempt to implement everything blindly in one step.

Follow this order.

Phase 1 — Project Foundation

Implement:

- Android project configuration
- Kotlin
- Jetpack Compose
- Dependencies
- Navigation
- Theme
- Basic architecture
- Inspect "demo.html"

At the end, make sure the project builds successfully.

---

Phase 2 — UI Recreation

Recreate the "demo.html" interface using native Compose.

Implement:

- Main layout
- Navigation
- Chat UI
- Sidebar/history
- Model selector
- Input bar
- Cards
- Buttons
- Settings
- Profile

Focus on visual accuracy.

Do not connect APIs yet.

---

Phase 3 — Firebase Authentication

Implement:

- Firebase configuration
- Email registration
- Login
- Google
- GitHub
- Logout
- Authentication state

Test every authentication flow.

---

Phase 4 — Firestore

Implement:

- User profile
- Chat collection
- Messages
- Saved chats
- Chat history
- Rename/delete
- Security rules

Test user isolation.

---

Phase 5 — AgentRouter

Implement:

- AI service
- Base URL
- API authentication
- Model configuration
- Request/response handling
- Streaming if supported
- Error handling

Initially test with ONE model.

After the API works correctly, add the remaining models.

---

Phase 6 — Complete Chat

Connect:

UI
↓
ViewModel
↓
Repository
↓
AgentRouter

Implement:

- Real AI response
- Context/history
- Loading
- Retry
- Regenerate
- Stop generation if supported
- Firestore persistence

---

Phase 7 — Polish

Add:

- Markdown
- Code syntax presentation
- Copy buttons
- Search
- Rename
- Delete confirmation
- Saved chats
- Animations where appropriate
- Empty states
- Error states

Make sure everything still matches "demo.html".

---

Phase 8 — Testing

Test:

Authentication

- Sign up
- Login
- Logout
- Google
- GitHub

Chat

- New chat
- Send message
- Receive response
- Switch model
- Retry
- Regenerate

Database

- Save
- Load
- Rename
- Delete
- Saved chats

Network

- Offline
- Timeout
- API error
- Invalid response

Security

- Verify users cannot access other users' Firestore data.

---

19. IMPORTANT RULES FOR THE CODING AGENT

1. Inspect before modifying.
2. Read "demo.html" before building the UI.
3. "demo.html" is the visual source of truth.
4. Build the Android UI natively with Compose.
5. Do not replace the design with a generic AI-chat template.
6. Do not invent API model IDs.
7. Keep API configuration centralized.
8. Do not put network calls inside Composables.
9. Do not store passwords in Firestore.
10. Do not use insecure Firestore rules.
11. Do not create fake AI responses.
12. Test each phase before moving to the next.
13. Keep the project buildable after every major phase.
14. Do not delete working code unnecessarily.
15. If something is missing, report exactly what is missing instead of guessing.

---

20. FIRST ACTION

Start by inspecting the project.

Specifically inspect:

- "demo.html"
- Existing Kotlin files
- Gradle configuration
- AndroidManifest
- Existing resources
- Firebase configuration if present
- Existing assets

Then create a concise implementation plan based on the actual project.

Do not start writing the entire application immediately.

First understand the existing project and "demo.html", then begin with Phase 1.