# Diplom Project: Ivanovo Music Theatre App

## Project Overview
This project is an Android application developed as a diploma project. It serves as a mobile client for the **Ivanovskyi Muzykalnyi Teatr** (Ivanovo Music Theatre), providing users with up-to-date information about upcoming performances and news.

The app is built using modern Android development practices, including **Jetpack Compose** for the UI, **Hilt** for dependency injection, and **Jsoup** for web scraping from the official theatre website ([ivmuz.ru](https://www.ivmuz.ru)).

### Main Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Networking**: Retrofit & OkHttp
- **Data Parsing**: Jsoup (Web scraping)
- **Data Persistence**: DataStore (Preferences)
- **Image Loading**: Coil (likely, though not explicitly seen in build.gradle, it's standard for Compose)

### Architecture
The project follows a standard **MVVM (Model-View-ViewModel)** architecture:
- `data/`: Contains models, repositories, and remote data sources (Scraper).
- `di/`: Hilt modules for providing dependencies.
- `ui/`: Compose screens, components, view models, and theme definitions.

## Building and Running
The project uses the Gradle build system.

### Build Commands
- **Build APK**: `./gradlew assembleDebug`
- **Clean Project**: `./gradlew clean`
- **Install on Device**: `./gradlew installDebug`
- **Run Unit Tests**: `./gradlew test` (Note: No tests currently implemented)

### Configuration
- **Namespace**: `com.sianov.stepan`
- **Min SDK**: 30
- **Target/Compile SDK**: 36
- **JDK Version**: 17

## Key Features
- **Poster Screen**: Displays upcoming performances scraped from the "Ticket Online" section of the theatre website.
- **News Screen**: Displays latest news from the theatre.
- **Settings Screen**: Allows users to toggle between Light and Dark themes and adjust the font size multiplier.
- **Edge-to-Edge UI**: Uses modern Android 15+ edge-to-edge support.

## Development Conventions
- **UI**: All UI should be implemented using Jetpack Compose.
- **DI**: Use Hilt for all dependency injections.
- **State Management**: Use `StateFlow` in ViewModels to expose UI state.
- **Dependency Management**: Centralized in `gradle/libs.versions.toml`.
- **Scraping**: Logic for parsing HTML from `ivmuz.ru` is contained within `IvMuzScraper.kt`.
