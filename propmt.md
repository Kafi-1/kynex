Build a native Android AI chat application called Kynex AI using Kotlin + Jetpack Compose.

1. Project Goal

Kynex AI is a multi-model AI chat application.

The app should allow users to:

- Create an account
- Sign in
- Continue with Google
- Continue with GitHub
- Start new AI conversations
- Select different AI models
- Send messages and receive AI responses
- Save chat history
- View previous conversations
- Rename/delete conversations
- Save/bookmark important chats
- Switch AI models
- Manage basic account/app settings

The application is intended primarily for personal use and a very small number of users initially.

Do NOT build a web app.
Do NOT use React.
Do NOT use Flutter.
Build a proper native Android application using Kotlin.

---

2. UI / UX

The UI should be implemented in Jetpack Compose.

The visual design must follow the existing HTML UI/UX reference provided by the project owner.

Keep the same:

- Theme
- Colors
- Typography
- Spacing
- Cards
- Buttons
- Navigation style
- Chat bubbles
- Input area
- Model selector
- Dark/light appearance where applicable

Do not redesign the visual identity unnecessarily.

Create reusable Compose components instead of putting the entire UI inside one screen.

Recommended screens:

1. Splash
2. Login
3. Sign Up
4. Authentication provider selection
5. Home / Chat
6. New Chat
7. Chat Conversation
8. Chat History
9. Saved Chats
10. Profile
11. Settings
12. Model Selection
13. About

---

3. Authentication

Use Firebase Authentication.

Support:

- Email + Password Sign Up
- Email + Password Login
- Google Sign-In
- GitHub Sign-In
- Logout
- Session persistence
- Authentication state checking
- Basic error handling

After successful authentication, create/update the user's Firestore profile.

User profile example:

users/{uid}

{
name,
email,
photoUrl,
provider,
createdAt,
updatedAt
}

Never store passwords manually in Firestore.

---

4. Database

Use Cloud Firestore.

The database should be designed around users, chats, messages and saved conversations.

Recommended structure:

users/{uid}

profile fields

chats/{chatId}
    title
    modelId
    createdAt
    updatedAt
    isSaved
    messageCount

    messages/{messageId}
        role
        content
        modelId
        createdAt
        isError

settings/{document}

    selectedModel
    theme
    etc.

Each user's data must be isolated by Firebase Security Rules.

A user must NEVER be able to read or modify another user's chats.

---

5. AI Model System

Implement a provider-independent AI client architecture.

Do not hard-code AI request logic directly inside Compose UI.

Use something similar to:

UI
↓
ViewModel
↓
Repository
↓
AI Service
↓
AgentRouter API
↓
Selected Model

Create a clean model abstraction so additional models can be added later without rewriting the chat screen.

Initial model list:

- GPT-5.6-Sol
- Claude Opus 4.8
- Claude Opus 5
- DeepSeek V4 Flash
- GLM 5.3

IMPORTANT:

Treat the displayed model name and the actual API model ID as separate values.

Example:

ModelInfo(
id = "...",
displayName = "Claude Opus 5",
provider = "Anthropic"
)

Do not assume the display name is the API model ID.

Keep model configuration centralized.

---

6. AgentRouter API

The application will communicate with AgentRouter.

Base URL:

https://agentrouter.org/

Use the API format supported by AgentRouter.

Implement:

- Base URL configuration
- API authentication
- Model selection
- Message history
- System prompt support
- Streaming response if supported
- Non-streaming fallback
- HTTP timeout
- Network error handling
- API error handling
- Loading state
- Retry handling

Use a proper HTTP client such as OkHttp/Retrofit or another stable Kotlin networking implementation.

Do not put API calls inside Composable functions.

---

7. API Configuration

For the first version, keep the API configuration centralized so it can easily be changed and the APK can be rebuilt later.

Example concept:

AiConfig
baseUrl
apiKey
models

Do NOT duplicate the API key throughout the source code.

Make model configuration easy to modify from one location.

IMPORTANT SECURITY NOTE:

The initial application is intended for limited personal use. The owner understands that credentials embedded in an APK cannot be considered fully secret.

Do not create unnecessary backend infrastructure for the AI API at this stage.

Firebase will still be used for authentication and user data.

---

8. Chat System

Implement a modern AI chat experience.

Requirements:

- User message bubble
- AI response bubble
- Markdown rendering
- Code block rendering
- Copy response
- Copy code
- Regenerate response
- Stop generation when streaming is supported
- Retry failed request
- Auto-scroll
- Loading indicator
- Empty state
- Error state
- Long-response support
- Message timestamps
- Selected model indicator

The conversation should be persisted to Firestore.

When a message is successfully sent:

1. Save user message
2. Send request to AgentRouter
3. Receive AI response
4. Display response
5. Save AI response to Firestore
6. Update chat metadata

Do not save duplicate messages when a request is retried.

---

9. Chat History

Create a history screen.

Show:

- Chat title
- Last message preview
- Last updated time
- Selected model
- Saved indicator

Support:

- Open conversation
- Rename conversation
- Delete conversation
- Save/unsave conversation
- Search conversations

Sort conversations by latest updated time.

---

10. Saved Chats

Create a Saved Chats section.

Users can save important conversations.

Saved conversations should remain available even after restarting the app.

Use Firestore as the source of truth.

---

11. State Management

Use a clean architecture.

Recommended:

Presentation
Compose UI
ViewModel

Domain
Models
Use cases/interfaces where useful

Data
Firestore repository
Authentication repository
AI repository
Network service

Do not over-engineer the project with unnecessary abstractions.

Keep the architecture understandable and maintainable.

Use Kotlin Coroutines and Flow where appropriate.

---

12. Offline / Loading Behaviour

The application should handle:

- No internet
- Slow internet
- API timeout
- Firebase timeout
- Invalid API response
- Authentication failure
- Empty response
- Rate limit
- Server error

Show user-friendly error messages.

Never crash because of an API/network failure.

---

13. Security Rules

Create Firestore security rules so that:

- Unauthenticated users cannot access user data.
- Authenticated users can access only their own data.
- users/{uid} can only be accessed by that same uid.
- chats and messages inherit the user's ownership.
- Users cannot modify another user's documents.

Do not use insecure rules such as:

allow read, write: if true;

---

14. Local Storage

Use local storage only where appropriate.

Store things such as:

- Theme preference
- Last selected model
- Temporary UI preferences

Do not use local storage as the primary permanent chat-history database.

Firestore should be the source of truth for authenticated users' chat history.

---

15. Performance

The application must work smoothly on relatively low-end Android devices.

Avoid:

- Heavy unnecessary animations
- Huge memory allocations
- Loading the entire chat history at once
- Unnecessary Firestore reads
- Recomposition caused by poor state handling

Use pagination or limited history loading where appropriate.

---

16. Project Quality

Write production-quality Kotlin.

Requirements:

- Null-safe Kotlin
- Coroutines
- Proper lifecycle handling
- No memory leaks
- No blocking network calls on the main thread
- Proper exception handling
- Reusable Compose components
- Meaningful names
- Minimal comments, only where useful
- No placeholder implementation left behind

Do not generate fake AI responses.

The actual AgentRouter API must be used.

---

17. Development Order

Implement the project in this order:

Phase 1:

- Create Android project
- Configure Kotlin/Compose
- Configure Firebase
- Configure authentication
- Create navigation structure

Phase 2:

- Build Login
- Sign Up
- Google authentication
- GitHub authentication
- Authentication state

Phase 3:

- Create Firestore user profile
- Create database repositories
- Create security rules

Phase 4:

- Build main chat UI
- Build model selector
- Build message components

Phase 5:

- Implement AgentRouter API
- Implement model switching
- Implement real AI responses

Phase 6:

- Save conversations to Firestore
- Build chat history
- Build saved chats

Phase 7:

- Add retry/regenerate/copy/search/delete/rename
- Add error handling
- Improve performance

Phase 8:

- Test the complete application
- Fix crashes
- Fix authentication issues
- Fix Firestore permission issues
- Fix API/network issues
- Prepare release APK

---

18. Important Development Rule

Before writing large amounts of code, inspect the existing project structure and existing HTML UI reference.

Do not destroy existing working code unnecessarily.

If a required configuration, Firebase project detail, API model ID, or design asset is missing, clearly identify exactly what is missing instead of inventing it.

Build the application incrementally and keep every phase runnable.

Start with Phase 1 now.