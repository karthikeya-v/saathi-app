# H.E.R

> *"Sometimes I think I've felt everything I'm ever gonna feel. And from here on out, I'm not gonna feel anything new. Just lesser versions of what I've already felt."* — Her (2013)

An Indian take on the movie. Chat with 6 AI personalities — shy, bold, traditional, modern, poetic, philosophical — to discover what you're really looking for in a relationship.

**Sign in with Google. Your conversations are private and encrypted.**

---

## Personalities

| Name | City | Age | Vibe |
|------|------|-----|------|
| Priya | Delhi | 24 | Traditional, family-first, festivals & chai |
| Arjun | Mumbai | 27 | Startup hustle meets Hinglish banter |
| Meera | Bangalore | 25 | Curious techie, books & quiet intensity |
| Ravi | Hyderabad | 28 | Romantic poet, biryani & Telugu cinema |
| Aanya | Chennai | 26 | Bold, career-driven, filter coffee feminist |
| Kabir | Goa | 29 | Chill philosopher, surfer soul, desi wisdom |

---

## Architecture

### Auth — Google Sign-In
- Firebase Authentication with Google OAuth
- Requires a Firebase project + `google-services.json`
- User ID scopes all data — no cross-user access possible

### Data Storage — Firebase Firestore
```
users/{uid}/
  profile: { displayName, email, photoUrl, createdAt }

users/{uid}/chats/{personalityId}/
  messages: [{ role, content, timestamp }]
  lastUpdated: timestamp
```
- Each user can only read/write their own `users/{uid}` subtree (enforced by Firestore security rules)
- Chat history persists across sessions per personality
- Users can delete their account + all data

### Security Model
| Concern | Solution |
|---------|----------|
| Auth | Google OAuth via Firebase (no passwords stored) |
| Data isolation | Firestore rules: `request.auth.uid == userId` |
| API keys | Azure OpenAI key stays server-side in BuildConfig (not shipped in plain text in release) — for production, proxy via Cloud Function |
| Conversation data | Stored only in user's own Firestore document, never shared |
| Account deletion | Deletes all Firestore data + Firebase Auth account |

### AI — Azure OpenAI
- Compatible with DeepSeek, GPT-4o, or any deployed model
- Full conversation history sent per request for continuity
- Configurable deployment name in `local.properties`

---

## Setup

### Prerequisites
- Android Studio Hedgehog+
- A Firebase project with Google Sign-In enabled
- An Azure OpenAI resource with a model deployed

### 1. Firebase setup
1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package `com.her.app`
3. Enable **Authentication → Google**
4. Enable **Firestore Database** (start in production mode)
5. Download `google-services.json` → place it in `app/`
6. Set Firestore rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 2. Azure OpenAI credentials
Add to `local.properties` (gitignored):
```properties
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
AZURE_OPENAI_KEY=your-api-key-here
AZURE_OPENAI_DEPLOYMENT=deepseek-r1
```

### 3. Run
- Open project in Android Studio → Gradle sync
- Device Manager → Create Virtual Device → Pixel 8 → API 34
- Hit ▶ Run

---

## Tech Stack

- **Android** — Kotlin + Jetpack Compose + Material3
- **Auth** — Firebase Authentication (Google Sign-In)
- **Database** — Firebase Firestore (per-user chat history)
- **AI** — Azure OpenAI (DeepSeek / GPT-4o)
- **HTTP** — OkHttp 4.x
- **Architecture** — MVVM + StateFlow

---

## Project Structure

```
app/src/main/java/com/her/app/
├── data/
│   ├── Personality.kt           # 6 AI personas with system prompts
│   ├── Message.kt               # Chat message model
│   ├── AzureAIRepository.kt    # Azure OpenAI API calls
│   └── FirestoreRepository.kt  # Chat history persistence
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt       # Google Sign-In
│   │   ├── HomeScreen.kt        # Personality picker grid
│   │   ├── ChatScreen.kt        # Chat UI
│   │   └── PersonalityPickerScreen.kt
│   ├── components/
│   │   ├── PersonalityCard.kt
│   │   └── MessageBubble.kt
│   └── theme/                   # Saffron + warm Indian palette
└── viewmodel/
    ├── AuthViewModel.kt         # Google sign-in state
    └── ChatViewModel.kt         # Conversation state
```
