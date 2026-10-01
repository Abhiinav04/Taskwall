# TaskWall

TaskWall is a production-quality Android application whose defining feature is a dynamic home-screen live wallpaper displaying the user's current tasks.

## Philosophy
TaskWall is simple, fast, offline-first, visually premium, and AMOELD-friendly. The application focuses on manual task management and an always-visible state via a Live Wallpaper, Widgets, and the App interface.

## Core Features
1. **Live Wallpaper**: A lightweight Canvas renderer that projects your today tasks, time, date, and a daily quote on your home screen.
2. **App Widget**: A Glance-based widget for checking tasks quickly.
3. **Daily Rollover**: Uncompleted tasks automatically carry forward at midnight without user intervention.
4. **Quotes**: A daily rotation of intellectually stimulating quotes.
5. **Local Data**: Complete privacy. Uses Room and DataStore to keep everything offline.

## Architecture
- UI: Jetpack Compose + Material 3
- Data: Room Database (KSP) + Flow
- Preferences: DataStore
- Live Wallpaper: WallpaperService + Custom Canvas Engine
- Widget: AndroidX Glance

## Development
To build the project:
`./gradlew assembleDebug`

## Testing
Unit and UI tests will be written in the `test` and `androidTest` folders, respectively.
