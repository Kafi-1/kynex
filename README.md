# Kynex AI

Native Android multi-model AI chat app — Kotlin + Jetpack Compose.
UI recreated from `demo.html` (the visual source of truth).

## What's implemented

- Email/password, Google and GitHub sign-in (Firebase Auth)
- Persistent sessions + auto profile creation in Firestore
- Multi-model AI chat via AgentRouter (streaming SSE + non-streaming fallback)
- Model selector (GPT-5.6-Sol, Claude Opus 4.8, Claude Opus 5, DeepSeek V4 Flash, GLM 5.3)
- Markdown rendering, code blocks with copy button, copy message, regenerate, retry, stop generation
- Chat history with date grouping, search, rename, delete, save/bookmark (Firestore-synced)
- Saved chats screen, profile screen, settings (theme, default model, logout), about
- **Image Generation** via Pollinations (`nanobanana-2`, configurable) — natural-language prompts,
  loading/error states, retry, Save (gallery) + Share actions
- Light/dark theme matching demo.html
- Firestore security rules (`firestore.rules`) enforcing per-user data isolation

## Image Generation configuration

The Pollinations App Key is **never committed**. It flows like this:

```
local.properties (gitignored)          GitHub Actions
        │                                    │
        │                            secret: POLLINATIONS_APP_KEY
        ▼                                    ▼
  app/build.gradle.kts  →  BuildConfig.POLLINATIONS_APP_KEY
                                   ▼
        data/network/ImageGenConfig.kt  (centralized)
```

- **Local builds:** put the key in `local.properties`:
  `POLLINATIONS_APP_KEY=pk_...`
- **GitHub Actions:** create repository secret **`POLLINATIONS_APP_KEY`**
  (Settings → Secrets and variables → Actions → New repository secret).
  The workflow writes it into `local.properties` before building.
- **Model/size:** change `DEFAULT_MODEL` / `DEFAULT_SIZE` in
  `app/src/main/java/com/kynex/ai/data/network/ImageGenConfig.kt`.
- Auth uses the official Pollinations flow: `Authorization: Bearer <app key>`
  (per the gen.pollinations.ai OpenAPI spec).

## Project layout

```
app/src/main/java/com/kynex/ai/
├── KynexApp.kt, MainActivity.kt
├── core/theme/          KynexTheme (demo.html colors)
├── di/AppGraph.kt       manual dependency container
├── domain/model/        AiModel, Chat, ChatMessage, UserProfile
├── data/
│   ├── auth/            AuthRepository (Firebase)
│   ├── firestore/       ChatRepository (chats/messages/profile)
│   ├── network/         AiConfig + AgentRouterService (OkHttp, streaming)
│   └── prefs/           SettingsDataStore (theme, last model)
└── presentation/
    ├── navigation/      NavHost, routes
    ├── components/      markdown, chat components, auth components
    └── screens/         splash, auth, chat, history, saved, settings, profile, about
```

## Before you build — 3 required actions

### 1. Firebase config

`app/google-services.json` is now the real config for project **kyenx-1**
(package `com.kynex.ai`). Still needed in the
[Firebase Console](https://console.firebase.google.com):

1. Enable **Authentication** → Sign-in methods: Email/Password, Google, GitHub
2. For Google Sign-In: add your app's **SHA-1** fingerprint
   (Android Studio → Gradle panel → app → Tasks → android → signingReport)
3. Create a **Cloud Firestore** database
4. Deploy the security rules: paste the contents of `firestore.rules`
   into Firestore Console → Rules → Publish

### 2. Paste your AgentRouter API key

Open `app/src/main/java/com/kynex/ai/data/network/AiConfig.kt` and set:

```kotlin
const val API_KEY = "PASTE_YOUR_AGENTROUTER_API_KEY_HERE"
```

### 3. Verify model IDs

`AiConfig.MODELS` holds the model list. The `id` values are the API model IDs
sent to AgentRouter — **check them against your AgentRouter account's model
list** and correct them there if needed (display names stay unchanged).
If any ID is wrong the API will return an error message, which the app shows
with a Retry option.

Also paste your Google Web Client ID in
`data/auth/AuthRepository.kt` → `AuthConfig.GOOGLE_WEB_CLIENT_ID`
(Firebase Console → Authentication → Sign-in method → Google → Web SDK config).

## Build

Open this folder in **Android Studio** (Koala or newer). It will sync Gradle
automatically (the wrapper config is included). Then:

- Debug: Run ▶ (or `./gradlew assembleDebug`)
- Release APK: Build → Generate Signed App Bundle / APK

Note: until the placeholder `google-services.json` is replaced, the app
**builds but sign-in will fail at runtime**. The AI chat also needs the real
API key. With real credentials everything is fully functional.

## Requirements

- Android Studio with AGP 8.5+, Kotlin 2.0
- Min SDK 26 (Android 8.0), target SDK 34
- Internet connection
