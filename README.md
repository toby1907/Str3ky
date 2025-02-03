## Str3ky
A  productivity and time management app designed to help users build positive habits, stay focused, and achieve their goals through structured focus sessions, streak tracking, and a rewarding achievement system
## Features
- Goal Setting: Users can define specific goals or challenges they want to achieve.
- Structured Focus Sessions: Users can initiate timed focus sessions based on their goals.
- Alarm Reminders: Users can set alarms to remind them to start their focus sessions.
- Progress Tracking: The app tracks the user's progress towards their goals.
- Streak Management: The app maintains a streak counter to encourage daily engagement.
- Achievement System: The app features a built-in achievement system to reward users for their progress.
- Streak Milestones: Reaching consecutive day milestones (e.g., 3 days, 7 days, 30 days).
- Total Hours Spent: Accumulating total hours in focus sessions (e.g., 10 hours, 50 hours, 100 hours).
- Data Persistence: The app uses Room, an Android persistence library, to store user data, goals, and achievements
- User Data: The app stores user data such as total hours spent, achievements unlocked and longest streak



## Technology Stack
- **[Kotlin](https://kotlinlang.org/)**: The official programming language for developing Android applications.
- **[Jetpack Compose](https://developer.android.com/develop/ui/compose)**: A modern UI toolkit for building Android applications in Kotlin.
- **[Lottie](https://github.com/LottieFiles/dotlottie-android)**: A lightweight image loading library.
- **[Dagger Hilt](https://dagger.dev/hilt/)**: Dependency Injection Framework.
- **[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)**: A lifecycle-aware Android Architecture Component for holding state.
- **[Room Persistence Library](https://developer.android.com/training/data-storage/room)**: Android Jetpack Library for local data caching.
- **[Datastore](https://developer.android.com/topic/libraries/architecture/datastore)**: Jetpack DataStore is a data storage solution that allows you store key-value pairs asynchronously.
- **[BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver)**:  a component that allows your application to listen for and respond to system-wide or application-specific broadcast announcements
- **[NotificationCompat](https://developer.android.com/reference/androidx/core/app/NotificationCompat)**: NotificationCompat is a class in the AndroidX Core library that helps you build notifications in a way that's compatible across different versions of Android


## Screenshots
| ::::::::::::::::::::::::::::::::::::::::  |    ::::::::::::::::::::::::::::::::::::::::    |  ::::::::::::::::::::::::::::::::::::::::   |     ::::::::::::::::::::::::::::::::::::::::      |        ::::::::::::::::::::::::::::::::::::::::         |             ::::::::::::::::::::::::::::::::::::::::              |
|:-----------------------------------------:|:----------------------------------------------:|:-------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------------:|:-----------------------------------------------------------------:|
| ![Home Screen](./screenshots/home2.1.jpg) | ![Progress screen](./screenshots/progress.jpg) | ![focus setting](./screenshots/setting.jpg) | ![focus session](./screenshots/focus_session.jpg) | ![Complete Screen](./screenshots/completion_screen.jpg) | ![Congratulation Screen](./screenshots/congratulation_screen.jpg) |

## Setup Instructions

1. **Clone the repository to your local machine.**
```bash
git clone  https://github.com/toby1907/Str3ky.git

cd Str3ky/
```
2. **Open in Android Studio**
- Open Android Studio
- Select `File` > `Open...`
```
3. **Build and run the app**
- Ensure your Android device or emulator is set up.
- Click on the `Run` button or use `Shift + F10`.


