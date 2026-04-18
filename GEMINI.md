# Diplom Project: Ivanovo Music Theatre App

## Project Overview
This project is a comprehensive mobile solution for the **Ivanovskyi Muzykalnyi Teatr** (Ivanovo Music Theatre), developed as a diploma project. It consists of an Android client and a Python-based backend server.

The project is structured into two main directories:
- `Client/`: The Android application (Kotlin, Jetpack Compose).
- `Server/`: The backend server (Django), handling web scraping and providing a JSON API for the client.

The server retrieves data by web scraping the official theatre website ([ivmuz.ru](https://www.ivmuz.ru)) and serves it as JSON, reducing the load on the mobile client.

### Main Technologies

#### Client (Android)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Dependency Injection**: Hilt
- **Networking**: Retrofit & OkHttp
- **Data Parsing**: Gson (for JSON) & Jsoup (legacy fallback)
- **Data Persistence**: DataStore
- **Architecture**: MVVM (Model-View-ViewModel)

#### Server (Backend)
- **Framework**: Django
- **Language**: Python
- **Scraping**: BeautifulSoup4 & Requests
- **API**: Standard Django Views returning JsonResponse

### Architecture
- `Client/`: Follows MVVM. Data is fetched from the Django server via Retrofit.
- `Server/`: Django project (`theatre_api`) with a `theatre` app.
  - `theatre/views.py`: Contains the logic for scraping and returning JSON.
  - `theatre/urls.py`: Defines the API endpoints (`/posters/`, `/news/`).

## Building and Running

### Server (Django)
1. Navigate to the `Server/` directory.
2. Install dependencies: `pip install -r requirements.txt`.
3. Run migrations (optional for this setup): `python manage.py migrate`.
4. Start the server: `python manage.py runserver 0.0.0.0:8000`.

### Client (Android)
1. Open the `Client/` directory in Android Studio.
2. Ensure the server is running (use `10.0.2.2:8000` for the emulator).
3. Build and run the app on an emulator or physical device.

## Key Features
- **Centralized Scraping**: The Django server handles all web scraping, providing clean JSON to the client.
- **Dynamic Poster**: Displays upcoming performances.
- **News Feed**: Provides latest news updates.
- **Settings**: Theme and font size customization.

## Development Conventions
- **Server**: All API logic should be in the `theatre` app views.
- **Client**: Use Retrofit with Gson for API calls. Use Hilt for DI.
- **Scraping**: If the theatre website structure changes, update the scraping logic in `Server/theatre/views.py`.
