# Baura - Android App

## Overview

Baura es la versión Android de la aplicación de descubrimiento de perfumes. Es un clon de la app iOS MyFragance (PerfBeta), implementado en Kotlin nativo con Jetpack Compose.

## Tech Stack

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth (Email, Google Sign-In)
- **Image Loading**: Coil
- **Local Storage**: DataStore Preferences + File Cache
- **Async**: Kotlin Coroutines + Flow

## Project Structure

```
app/src/main/java/com/jrlabs/baura/
├── app/                    # Application & MainActivity
├── config/                 # Configuration & Secrets
├── data/
│   ├── local/             # CacheManager, MetadataIndexManager
│   ├── model/             # Data models (Perfume, User, Profile, etc.)
│   │   └── enums/         # Enums (Gender, Season, Occasion, etc.)
│   ├── remote/            # Firebase services
│   └── repository/        # Repositories
├── di/                    # Hilt modules
├── ui/
│   ├── theme/             # Design System (Colors, Typography, Dimensions)
│   ├── components/        # Reusable UI components
│   │   ├── buttons/       # AppButton, etc.
│   │   ├── cards/         # PerfumeCard, ProfileCard
│   │   ├── inputs/        # Form inputs
│   │   └── feedback/      # Loading, Error states
│   ├── navigation/        # Navigation setup
│   └── screens/           # Feature screens
│       ├── auth/          # Login, Register
│       ├── home/          # Home tab
│       ├── test/          # Olfactory test
│       ├── library/       # Wishlist, Tried perfumes
│       ├── explore/       # Search & Filter
│       ├── settings/      # Settings & Profile
│       ├── splash/        # Splash screen
│       └── onboarding/    # Onboarding flow
└── utils/                 # Utilities (AppLogger, etc.)
```

## Key Features

1. **Authentication**: Email/Password and Google Sign-In
2. **Olfactory Test**: Personalized fragrance profiling
3. **Recommendations**: AI-powered perfume suggestions
4. **Library**: Wishlist and tried perfumes management
5. **Explore**: Search and filter perfumes
6. **Caching**: Permanent cache with incremental sync

## Design System

The design system mirrors the iOS DesignTokens.swift:

- **Colors**: AppColors (brand, background, text, feedback)
- **Typography**: AppTypography (Georgia for titles, System for body)
- **Spacing**: AppSpacing (8pt grid system)
- **Corner Radius**: AppCornerRadius
- **Elevation**: AppElevation

## Firebase Structure

Uses the same Firestore structure as iOS:

```
firestore/
├── perfumes/               # Perfume database
├── users/
│   ├── {userId}/
│   │   ├── profiles/       # User's olfactory profiles
│   │   ├── wishlist/       # Wishlist items
│   │   └── tried_perfumes/ # Tried perfumes
├── questions_es/           # Spanish questions
├── questions_en/           # English questions
├── brands/
├── families/
└── notes/
```

## Build & Run

1. Add `google-services.json` to `app/` folder
2. Open in Android Studio
3. Sync Gradle
4. Run on device/emulator (min SDK 26)

## Important Notes

- Cache version: v11 (must match iOS for compatibility)
- Supports Spanish (es) and English (en) localization
- Uses same algorithm as iOS for recommendations
- Shares Firestore data with iOS app

## Correspondence with iOS

| iOS (Swift) | Android (Kotlin) |
|-------------|-----------------|
| SwiftUI | Jetpack Compose |
| @MainActor | @HiltViewModel |
| Actor | Mutex + Coroutines |
| Combine | Kotlin Flow |
| Kingfisher | Coil |
| @EnvironmentObject | Hilt Injection |
| UserDefaults | DataStore |

## Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run lint
./gradlew lint
```
