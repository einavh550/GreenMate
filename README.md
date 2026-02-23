# GreenMate 🌱

**Smart Plant Care & Watering Reminder Android Application**

An academic Android project for the Mobile Application Development course. GreenMate helps users track watering and fertilizing schedules for all their plants, log care actions, and receive timely reminders so their garden always thrives.

---

## 📱 Features

### Core Features
- **Plant Management** - Add, edit, and delete plant profiles with custom names, locations, and photos
- **Care Scheduling** - Set custom watering and fertilizing intervals for each plant
- **Dashboard** - View today's care tasks and overdue items at a glance
- **Care History** - Track when each plant was watered or fertilized
- **Status Tracking** - Visual indicators show plant health status (Healthy, Needs Attention, Overdue)

### Extra Features (Beyond Basic Requirements)
- **🔔 Push Notifications** - Daily reminders via WorkManager background scheduling
- **📱 Home Screen Widget** - Quick access to pending care tasks without opening the app
- **🌙 Dark Mode** - Full light/dark theme support following system settings
- **📸 Camera Integration** - Take photos of plants or choose from gallery
- **🎯 Onboarding Flow** - First-time user tutorial with permission requests
- **🔥 Care Streak Tracking** - Gamification element tracking consecutive days of care
- **🔍 Search & Filter** - Find plants by name, filter by status or location
- **📊 Statistics** - Dashboard stats showing total plants, care streak, and weekly completions

---

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with **Repository pattern** for clean separation of concerns.

```
com.example.greenmate_project/
├── data/                    # Repository layer
│   ├── PlantRepository.kt          # Interface for plant operations
│   ├── PlantRepositoryImpl.kt      # Firestore implementation
│   ├── ActionRepository.kt         # Interface for care history
│   └── ActionRepositoryImpl.kt     # Firestore implementation
│
├── model/                   # Data models
│   ├── Plant.kt                    # Plant profile data class
│   ├── CareAction.kt               # Care history entry
│   ├── CareTask.kt                 # Computed task for dashboard
│   ├── ActionType.kt               # Enum: WATER, FERTILIZE
│   └── PlantStatus.kt              # Enum: HEALTHY, NEEDS_ATTENTION, OVERDUE
│
├── service/                 # Firebase services
│   ├── FirebaseAuthService.kt      # Anonymous authentication
│   └── FirestoreService.kt         # Database access helpers
│
├── ui/                      # UI layer (Activities, Fragments, ViewModels)
│   ├── splash/                     # Splash screen with animation
│   ├── onboarding/                 # First-time user tutorial
│   ├── dashboard/                  # Main dashboard with tasks
│   ├── plants/                     # Plant list, details, add/edit
│   └── settings/                   # App preferences
│
├── util/                    # Utility classes
│   ├── Constants.kt                # App-wide constants
│   ├── PreferencesManager.kt       # SharedPreferences wrapper
│   ├── NotificationHelper.kt       # Notification channel & display
│   ├── ImageUtils.kt               # Image loading/saving
│   └── AnimationUtils.kt           # Activity transition animations
│
├── widget/                  # Home screen widget
│   └── CareTasksWidget.kt          # AppWidgetProvider implementation
│
├── worker/                  # Background tasks
│   ├── CareReminderWorker.kt       # Daily notification worker
│   └── WorkerScheduler.kt          # WorkManager scheduling
│
├── GreenMateApp.kt          # Application class
└── MainActivity.kt          # Main activity with bottom navigation
```

---

## 🗄️ Firebase Structure

Using **Firebase Free Tier (Spark Plan)** with Anonymous Authentication and Cloud Firestore.

```
Firestore Database Structure:
└── users/
    └── {uid}/                      # Anonymous user ID
        └── plants/
            └── {plantId}/          # Auto-generated plant document
                ├── name: String
                ├── location: String
                ├── photoUrl: String?
                ├── waterIntervalDays: Int
                ├── fertilizeIntervalDays: Int
                ├── lastWateredAt: Timestamp?
                ├── lastFertilizedAt: Timestamp?
                └── createdAt: Timestamp
                │
                └── actions/        # Care history subcollection
                    └── {actionId}/
                        ├── type: String (WATER/FERTILIZE)
                        └── performedAt: Timestamp
```

### Why Anonymous Authentication?
- **Simplicity** - No login required, perfect for personal use app
- **Free tier** - No cost for unlimited anonymous users
- **Persistence** - User data persists until app data is cleared
- **Security** - Firestore rules ensure users only access their own data

---

## 📚 Libraries & Dependencies

| Library | Purpose |
|---------|---------|
| **Firebase Auth** | Anonymous user authentication |
| **Firebase Firestore** | Cloud database for plant data |
| **Material Design 3** | Modern UI components and theming |
| **AndroidX Lifecycle** | ViewModel and LiveData |
| **WorkManager** | Reliable background notification scheduling |
| **ViewPager2** | Onboarding page swiping |
| **FileProvider** | Secure camera photo sharing |

All libraries are managed via Gradle Version Catalog (`gradle/libs.versions.toml`).

---

## 🎨 UI/UX Design

- **Material Design 3** theming with custom green primary color palette
- **Consistent spacing** using dimension resources (`dimens.xml`)
- **Proper contrast ratios** verified for accessibility
- **Responsive layouts** supporting different screen sizes
- **Smooth animations** for activity transitions and UI feedback
- **Edge-to-edge display** with proper window insets handling
- **Dark mode support** with separate `values-night` resources

---

## 🚀 Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   ```

2. **Open in Android Studio** (Hedgehog or newer recommended)

3. **Firebase Setup**
   - The `google-services.json` file is included for this academic project
   - For your own project, create a Firebase project and download your own config

4. **Build and Run**
   - Connect an Android device or start an emulator (API 24+)
   - Click Run or press `Shift + F10`

---

## 📋 Requirements

- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36 (Android 16)
- **Kotlin**: 2.0.21
- **Android Studio**: Hedgehog (2023.1.1) or newer

---

## 📝 Academic Notes

This project demonstrates:

1. **Modern Android Development** - Kotlin, MVVM, LiveData, ViewModel
2. **Firebase Integration** - Authentication and Cloud Firestore
3. **Background Processing** - WorkManager for reliable notifications
4. **UI Best Practices** - Material Design 3, proper spacing, accessibility
5. **Code Organization** - Clean architecture with repository pattern
6. **App Widgets** - Home screen widget implementation
7. **Permissions Handling** - Runtime permissions for camera and notifications

---

## 👨‍💻 Author

Academic project for Android Mobile Application Development Course

**Submission Date**: February 2026

---

## 📄 License

This is an academic project. All rights reserved.
