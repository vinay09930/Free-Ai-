# FreeAI FIX_PLAN

## Complete Migration Strategy

### Phase 1: Architecture Reset
- Convert to properly separated Clean Architecture standard:
  - `domain`: Use cases, interfaces.
  - `data`: Repositories, Database, API.
  - `presentation`: UI, ViewModels, NavHost.

### Phase 2: Navigation Upgrade
- Replace lambda-based routing in `FreeAiApp` with `NavHost` and `rememberNavController()`.
- Add sealed classes for Routes: `Home`, `Chat`, `Providers`, `Models`, `KnowledgeBase`, `AIStudio`.

### Phase 3: Premium UI Glassmorphism
- Establish a consistent Material 3 Theme combined with custom `Modifier.glassCard()`.
- Upgrade the `HomeScreen` into a grand Dashboard.
- Upgrade `ChatScreen` with refined bubble metrics.

### Phase 4: Hub Implementations
- Wire `ProviderHubScreen` to display registered API providers.
- Wire `ModelHubScreen` to select and manage models.
- Wire `AIStudio` for parameterized prompting.
- Connect local room entities where possible.

### Phase 5: Testing
- Verify all routes.
- Address compilation warnings regarding AutoMirrored icons.
