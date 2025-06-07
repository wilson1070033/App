# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is an MLB Data Explorer Android application using Gradle with Kotlin DSL and Jetpack Compose.

### Essential Commands
- `./gradlew build` - Build the entire project
- `./gradlew app:assembleDebug` - Build debug APK
- `./gradlew app:assembleRelease` - Build release APK
- `./gradlew app:installDebug` - Install debug APK to connected device/emulator
- `./gradlew clean` - Clean build artifacts

### Testing Commands
- `./gradlew test` - Run unit tests
- `./gradlew app:testDebugUnitTest` - Run unit tests for debug variant
- `./gradlew connectedAndroidTest` - Run instrumented tests (requires device/emulator)
- `./gradlew app:connectedDebugAndroidTest` - Run instrumented tests for debug variant

### Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew app:lintDebug` - Run lint for debug variant

## Application Architecture

This is a **MLB Data Explorer** app that provides MLB game schedules/scores and player statistics using the official MLB Stats API.

### Architecture Pattern
- **MVVM (Model-View-ViewModel)** with Repository pattern
- **Single Activity** with Jetpack Compose navigation
- **Reactive UI** using StateFlow and Compose state management

### Key Architectural Layers

#### Data Layer
- **`MlbApiService`** - Retrofit interface for MLB Stats API (`https://statsapi.mlb.com/api/v1/`)
- **`NetworkModule`** - Singleton object providing configured Retrofit instance with logging interceptor and custom User-Agent
- **`MlbRepository`** - Single source of truth, handles API calls and data formatting
- **Data Models** - `Game.kt` and `Player.kt` containing data classes for API responses

#### Domain/Business Logic
- **`GamesViewModel`** - Manages game data state, date selection, and loading states
- **`PlayerViewModel`** - Handles player search, selection, and statistics with different stat types (hitting/pitching/fielding) and time ranges (season/career/yearByYear)

#### UI Layer
- **Navigation** - `MlbNavigation.kt` with sealed class `Screen` definitions for type-safe navigation
- **Three main screens**: `HomeScreen` (landing), `GamesScreen` (game schedules with date picker), `PlayerScreen` (player search and stats)
- **Material 3 theming** with custom color schemes and dynamic theming support

### MLB API Integration
- **Base URL**: `https://statsapi.mlb.com/api/v1/`
- **Key endpoints**: `/schedule` (games), `/people/search` (player search), `/people/{id}` (player stats)
- **Hydrate parameters**: Used to control API response depth (e.g., `"team,linescore"` for games, complex stats hydration for players)
- **Error handling**: Comprehensive error states in ViewModels with user-friendly messages

### State Management Patterns
- **Reactive state**: ViewModels use `StateFlow` with immutable data classes for UI state
- **Loading states**: Each screen handles loading, success, and error states appropriately
- **Navigation state**: Compose Navigation with proper back stack management

### Key Implementation Details
- **Date handling**: Uses `SimpleDateFormat` for API date formatting (`yyyy-MM-dd`) and display formatting
- **Smart cast handling**: Uses `?.let {}` blocks for nullable delegate properties to avoid compilation errors
- **JSON parsing**: Gson with lenient parsing to handle inconsistent API responses (e.g., `content.summary` field)
- **Network configuration**: 15-second timeouts, comprehensive logging, custom User-Agent header

### Build Configuration
- **Target SDK**: 35 (latest Android)
- **Min SDK**: 35 (cutting-edge only)
- **Compose**: Latest stable version with Kotlin Compose Compiler plugin
- **Dependencies**: Managed via Gradle version catalogs including Retrofit, Navigation Compose, Material 3

### Material Icons Usage
When adding new UI components, use these verified Material Icons that exist in the codebase:
- `Icons.Default.List`, `Icons.Default.Person`, `Icons.Default.Search`, `Icons.Default.Clear`
- `Icons.Default.ArrowBack`, `Icons.Default.DateRange`, `Icons.Default.Refresh`, `Icons.Default.Home`
- Avoid icons like `SportsBaseball`, `Sports`, `CalendarToday`, `Today` which don't exist in Material Icons

### API Rate Limiting and Error Handling
The MLB Stats API is public but implement proper error handling for network failures, parsing errors, and empty responses. The app gracefully handles cases where games or player data is unavailable.