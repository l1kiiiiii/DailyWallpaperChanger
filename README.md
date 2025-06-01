# Daily Wallpaper App

## Description

The Daily Wallpaper App automatically fetches and displays NASA's Astronomy Picture of the Day (APOD). Users can view the daily image and its title, and have the option to set the current image as their device's wallpaper. The app also schedules a daily background task to automatically update the wallpaper to the new APOD image, ensuring a fresh look every day.

## Features

*   **Daily Image Display**: Fetches and shows NASA's Astronomy Picture of the Day.
*   **Image Title**: Displays the title associated with the APOD.
*   **Set as Wallpaper**: Allows users to manually set the currently displayed image as their device wallpaper.
*   **Automatic Daily Wallpaper**: A background worker automatically fetches and sets the new APOD as the wallpaper once every 24 hours (network permitting).
*   **Modern Android Tech**: Built with Kotlin, Jetpack Compose for UI, WorkManager for background tasks, Retrofit for network calls, and Coil for image loading.

## Tech Stack

*   **Language**: Kotlin
*   **UI Toolkit**: Jetpack Compose
*   **Architecture**: MVVM (ViewModel for UI state management)
*   **Networking**:
    *   Retrofit: For making API calls to the NASA APOD API.
    *   OkHttp: Underlying HTTP client for Retrofit.
    *   Gson: For parsing JSON responses.
*   **Image Loading**: Coil
*   **Background Processing**: Jetpack WorkManager
*   **Permissions Handling**: Uses AndroidX Activity Compose for runtime permission requests (SET_WALLPAPER).

## Project Structure
## Setup and Configuration

1.  **Clone the repository:** git clone https://your-repository-url/DailyWallpaperApp.git 
2.  **API Key:**
    *   This app requires a NASA API key for fetching the APOD. You can get one from [https://api.nasa.gov/](https://api.nasa.gov/).
    *   Create a file named `secrets.properties` in the root directory of the project (at the same level as `build.gradle.kts` and `settings.gradle.kts`).
    *   Add your API key to `secrets.properties` like this:
    *   **Note**: `secrets.properties` is included in `.gitignore` to prevent your API key from being committed to version control.
3.  **Open in Android Studio:**
    *   Open Android Studio.
    *   Select "Open an Existing Project".
    *   Navigate to the cloned `DailyWallpaperApp` directory and open it.
4.  **Build and Run:**
    *   Let Android Studio sync Gradle and download dependencies.
    *   Select an emulator or connect a physical device.
    *   Click the "Run" button.

## Permissions

*   **`android.permission.INTERNET`**: Required to fetch images and data from the NASA APOD API.
*   **`android.permission.SET_WALLPAPER`**: Required to set the device wallpaper. The app will request this permission at runtime on Android 13 (API 33) and above when you try to set the wallpaper manually.
*   **`android.permission.ACCESS_NETWORK_STATE`**: Used by WorkManager to ensure the daily wallpaper update task only runs when the network is connected.

## How it Works

1.  **Main Activity (`MainActivity.kt`)**:
    *   Displays the UI using Jetpack Compose (`WallpaperApp` composable).
    *   Observes data from `WallpaperViewModel`.
    *   Provides a button (`SetWallpaperButton`) to manually set the wallpaper.
    *   Schedules the `WallpaperWorker` for daily updates.

2.  **ViewModel (`WallpaperViewModel.kt`)**:
    *   Fetches the APOD data (image URL and title) using `RetrofitInstance`.
    *   Exposes the image URL and title as observable states for the UI.

3.  **API Handling (`api/`)**:
    *   `ApodApiService.kt`: Defines the Retrofit service interface with the endpoint for fetching the APOD.
    *   `RetrofitInstance.kt`: Configures and provides a singleton instance of Retrofit.

4.  **Background Worker (`worker/WallpaperWorker.kt`)**:
    *   A `CoroutineWorker` managed by WorkManager.
    *   Scheduled to run once daily.
    *   Fetches the latest APOD URL using `RetrofitInstance`.
    *   Uses Coil to download the image as a Bitmap.
    *   Uses `WallpaperManager` to set the downloaded Bitmap as the device wallpaper.
    *   Retries if the network request or image processing fails.

## Further Improvements (TODO)

*   [ ] **Error Handling**: More robust error handling and user feedback (e.g., when API fails, image loading fails, wallpaper setting fails).
*   [ ] **Loading Indicators**: Better visual feedback during image loading or background operations.
*   [ ] **Image Quality Selection**: Allow users to choose image quality (e.g., HD URL from APOD if available).
*   [ ] **History**: Allow users to view past APOD images.
*   [ ] **Customization**: Options for when the daily update occurs or to disable it.
*   [ ] **Testing**: Add unit and instrumented tests.
*   [ ] **UI Enhancements**: Improve the visual design and user experience.
*   [ ] **Settings Screen**: For managing preferences like daily updates.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

