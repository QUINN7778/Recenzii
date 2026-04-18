# QWEN.md — Diplomi Project Context

## Project Overview

**Diplomi** is a diploma project — a mobile solution for the **Ivanovsky Music Theatre** (Ивановский Музыкальный Театр, ivmuz.ru). The system consists of three main parts:

1. **Client** — Android application (Kotlin, Jetpack Compose, MVVM architecture)
2. **Server** — Django backend (Python) with REST API and web scraping capabilities
3. **HTML files** — Static web pages (`main.html`, `geysha.html`) representing the theatre website source

The Android app retrieves data from the theatre website (`ivmuz.ru`) via the Django server, which provides clean JSON APIs. The app also includes user authentication, performance reviews, and settings customization.

---

## Directory Structure

```
Diplomi/
├── Client/                 # Android application (Kotlin)
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/sianov/stepan/
│   │       │   ├── data/           # Models, remote API, repositories
│   │       │   │   ├── model/      # Data classes (AppItem, User, PerformanceDetail, etc.)
│   │       │   │   ├── remote/     # ApiService.kt, IvMuzScraper.kt
│   │       │   │   └── repository/ # AppRepository, AuthRepository, SettingsRepository
│   │       │   ├── di/             # Hilt DI modules (NetworkModule.kt)
│   │       │   ├── ui/             # Screens, components, theme, viewmodels
│   │       │   │   ├── components/ # Reusable Compose components
│   │       │   │   ├── screens/    # Screen composables (Auth, Poster, News, Settings, etc.)
│   │       │   │   ├── theme/      # Material 3 theme (Color, Theme, Type)
│   │       │   │   └── viewmodel/  # ViewModels (Auth, Poster, News, Settings, PerformanceDetail)
│   │       │   ├── DiplomApp.kt    # Application entry point (Hilt Application class)
│   │       │   └── MainActivity.kt # Main activity
│   │       └── res/                # Android resources
│   ├── build.gradle.kts
│   └── gradle/
├── Server/                 # Django backend (Python)
│   ├── theatre/            # Django app (API views, models, urls)
│   │   ├── models.py       # Performance, Review, Ticket models
│   │   ├── views.py        # API endpoint logic (scraping, auth, reviews)
│   │   └── urls.py         # URL routing
│   ├── theatre_api/        # Django project settings
│   ├── manage.py
│   ├── requirements.txt
│   └── db.sqlite3
├── main.html               # Static HTML (theatre main page source)
├── geysha.html             # Static HTML (specific performance page)
└── links.txt               # (empty)
```

---

## Tech Stack

### Client (Android)
| Category       | Technology |
|----------------|------------|
| Language       | Kotlin |
| UI Framework   | Jetpack Compose (Material 3) |
| Architecture   | MVVM |
| DI             | Hilt |
| Networking     | Retrofit + OkHttp |
| JSON Parsing   | Gson |
| HTML Scraping  | Jsoup (IvMuzScraper.kt) |
| Data Persistence | DataStore (Preferences) |
| Navigation     | Navigation Compose |
| Image Loading  | Coil (via NetworkImage.kt) |
| Min SDK        | 30 (Android 11) |
| Target SDK     | 36 |
| JDK            | 17 |
| Namespace      | `com.sianov.stepan` |

### Server (Django)
| Category       | Technology |
|----------------|------------|
| Framework      | Django |
| REST API       | Django REST Framework |
| Web Scraping   | BeautifulSoup4 + Requests |
| Auth           | Token Authentication (authtoken) |
| Database       | SQLite (db.sqlite3) |
| CORS           | django-cors-headers |

---

## Key Features

### Android App
- **Poster Screen** — displays upcoming performances scraped from ivmuz.ru
- **News Screen** — latest theatre news
- **Performance Detail** — detailed view with reviews/ratings system
- **My Theatre Screen** — user's personal collection of saved performances
- **Authentication** — login/register via Django REST API
- **Settings** — theme toggle (light/dark), font size multiplier adjustment
- **Edge-to-Edge UI** — modern Android 15+ immersive UI support
- **Skeleton Loading** — shimmer placeholders during data loading

### Server API Endpoints
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/posters/` | GET | None | Get upcoming performances (scraped from ivmuz.ru) |
| `/auth/register/` | POST | None | Register new user (username, email, password) |
| `/auth/login/` | POST | None | User login, returns token |
| `/sync/` | POST | Token | Sync performance data to server archive |
| `/reviews/` | GET | None | Get reviews for a performance (`?url=...`) |
| `/reviews/` | POST | Token | Submit a review (rating + comment) |

---

## Building and Running

### Server (Django)
```bash
cd Server

# Create virtual environment (optional but recommended)
python -m venv venv
source venv/Scripts/activate  # On Windows

# Install dependencies
pip install -r requirements.txt

# Apply migrations
python manage.py migrate

# Start development server
python manage.py runserver 0.0.0.0:8000
```

### Client (Android)
```bash
cd Client

# Build debug APK
./gradlew assembleDebug

# Clean build
./gradlew clean

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test
```

> **Note:** When running on an Android emulator, use `10.0.2.2:8000` as the server base URL to reach the Django server on your machine's localhost. For physical devices on the same Wi-Fi, use your machine's local IP address (e.g., `192.168.1.X:8000`).

---

## Development Conventions

### Client (Android)
- **UI**: All UI implemented with Jetpack Compose — no XML layouts
- **DI**: Hilt for all dependency injection; modules defined in `di/NetworkModule.kt`
- **State Management**: `StateFlow` in ViewModels to expose UI state; `collectAsState()` in Compose
- **Navigation**: Navigation Compose with a single `NavHost`
- **Repository Pattern**: Data access abstracted through repository classes
- **Dependency Management**: Version catalog in `gradle/libs.versions.toml`
- **ProGuard**: Enabled for release builds (`proguard-rules.pro`)

### Server (Django)
- **API Design**: Use DRF `@api_view` decorators with `@permission_classes`
- **Permission Defaults**: Public endpoints use `AllowAny`, user-specific use `IsAuthenticated`
- **Scraping**: All scraping logic in `theatre/views.py` — extract data from ivmuz.ru HTML
- **Models**: Use the URL as the unique identifier for performances
- **CORS**: Ensure `django-cors-headers` is configured if the app accesses the server directly

---

## Important Files Reference

| File | Purpose |
|------|---------|
| `Client/app/src/main/java/.../data/remote/ApiService.kt` | Retrofit interface — all HTTP API calls defined here |
| `Client/app/src/main/java/.../data/remote/IvMuzScraper.kt` | Jsoup-based scraper — fallback for direct ivmuz.ru parsing |
| `Client/app/src/main/java/.../data/repository/AppRepository.kt` | Main data repository — combines API + scraper |
| `Client/app/src/main/java/.../data/repository/AuthRepository.kt` | Authentication repository — login, register, token management |
| `Client/app/src/main/java/.../data/repository/SettingsRepository.kt` | Settings persistence via DataStore |
| `Client/app/src/main/java/.../ui/viewmodel/PosterViewModel.kt` | ViewModel for poster screen — fetches and exposes performances |
| `Client/app/src/main/java/.../ui/viewmodel/AuthViewModel.kt` | ViewModel for auth — login/register state |
| `Client/app/src/main/java/.../ui/viewmodel/PerformanceDetailViewModel.kt` | ViewModel for performance detail — reviews, sync |
| `Client/app/src/main/java/.../ui/viewmodel/NewsViewModel.kt` | ViewModel for news feed |
| `Client/app/src/main/java/.../ui/viewmodel/SettingsViewModel.kt` | ViewModel for settings — theme, font size |
| `Client/app/src/main/java/.../di/NetworkModule.kt` | Hilt module — provides Retrofit, OkHttpClient, base URL |
| `Server/theatre/views.py` | All Django API endpoint handlers |
| `Server/theatre/models.py` | Django ORM models: Performance, Review, Ticket |
| `Server/theatre/urls.py` | URL → view mappings |
| `Server/requirements.txt` | Python dependencies: django, djangorestframework, beautifulsoup4, requests, django-cors-headers |

---

## Project Namespace & Package

- **Android namespace**: `com.sianov.stepan`
- **Application ID**: `com.sianov.stepan`
- **Version**: 1.0 (versionCode: 1)
