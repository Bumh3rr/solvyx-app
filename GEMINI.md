# GEMINI.md - Project Context & Instructions: Solvyx

## Project Overview
Solvyx is a specialized Android application designed to provide comprehensive support for individuals managing substance use. Built with modern Android technologies, it focuses on emotional well-being, harm reduction, and crisis management through an interactive and empathetic interface.

### Core Features
- **Berto (Emotional Robot):** An AI assistant (currently simulated) with multiple emotional states (Happy, Worried, Calm, etc.) that interacts with users.
- **ASSIST Diagnosis:** Implementation of the WHO ASSIST tool for substance use risk evaluation.
- **Emotional Log (Bitácora):** Daily tracking of mood, substance consumption, and anxiety levels.
- **First Aid Guides:** Interactive guides for crisis management, panic attacks (including a 5-4-3-2-1 grounding exercise with TTS), and craving management.
- **Decision Trees:** Logic-driven interaction paths for craving and information scenarios.
- **Support Network:** Management of SOS contacts.

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Dependency Injection:** Hilt
- **Local Persistence:** Room (SQL) and DataStore (Preferences)
- **Async Processing:** Kotlin Coroutines & Flow
- **Animations:** Lottie Compose
- **Visual Effects:** Haze (Blur)
- **Design System:** Custom theme using the **Nunito** font family and a specific "Teal" palette.

---

## Building and Running

### Key Commands
- **Assemble Debug APK:** `./gradlew assembleDebug`
- **Run Unit Tests:** `./gradlew test`
- **Run Instrumented (UI) Tests:** `./gradlew connectedAndroidTest`
- **Clean Project:** `./gradlew clean`
- **Build and Install:** `./gradlew installDebug`

### Environment Requirements
- **minSdk:** 24 (Android 7.0)
- **targetSdk:** 36
- **Java Version:** JDK 11 (source and target compatibility)

---

## Project Structure

```
app/src/main/java/com/solvyx/
├── backend/
│   ├── data/local/       → Room DB (DAO, Database, Entities)
│   ├── decisiontree/     → Engine, models, and predefined trees (alcohol, cristal)
│   ├── models/           → Domain models (Pregunta, Sustancia, etc.)
│   ├── presentation/     → ViewModels
│   └── repository/       → Data access logic (DiagnosticoRepository, DecisionTreeRepository)
├── di/                   → Hilt modules (AppModule)
├── ui/
│   ├── components/       → Reusable UI units (Buttons, TextFields, Drawer)
│   ├── diagnostico/      → ASSIST survey flow and screens
│   ├── navigation/       → NavGraph, Routes
│   ├── screens/          → Main feature screens (Home, Berto, Bitácora, Guías, etc.)
│   └── theme/            → Colors, Typography (Nunito), Shapes, Theme definitions
└── MainActivity.kt       → Entry point and root navigation setup
```

---

## Development Conventions

### Coding Style & Architecture
1.  **MVVM:** Always separate UI (Compose) from logic (ViewModel) and data (Repository).
2.  **Hilt DI:** Use `@HiltViewModel` for ViewModels and `@Inject` for dependencies. Use `AppModule.kt` for provider methods.
3.  **UI Components:** Prefer reusing components from `ui/components/common` (e.g., `SolvyxButton`, `SolvyxTextField`) to maintain visual consistency.
4.  **String Resources:** Use `strings.xml` for all user-facing text to support potential localization.
5.  **State Management:** Use `StateFlow` or `MutableState` within ViewModels to drive UI updates. Use `collectAsStateWithLifecycle()` in Compose.

### Testing Practices
- **Unit Tests:** Located in `app/src/test/`. Focus on ViewModels and Repositories.
- **UI Tests:** Located in `app/src/androidTest/`. Focus on critical user flows (Login, Survey, SOS).

### Design Guidelines
- **Font:** Nunito (Bold for headers, Regular for body).
- **Colors:** Use the defined `TealPrimary`, `TealDark`, and `BackgroundApp` tokens.
- **Berto:** When implementing interactions, reflect Berto's state in his visor color and illustration.

---

## Important Documentation
- `proyecto.md`: Detailed feature specifications, architecture diagrams, and design system tokens. Refer to this for specific UI requirements and flow details.
