Quick Dev README — Testing & debugging from PowerShell (adb + Gradle)

Purpose

This file collects the exact PowerShell commands and short explanations you can copy/paste to build, install, and capture logs for this Android project from a Windows PowerShell session (outside Android Studio). It also contains safe notes about Room migrations and a dev-only destructive fallback you can enable for fast iteration.

Checklist (high level)

- Ensure a JDK is available and JAVA_HOME is set (or use Android Studio bundled JDK).
- Ensure Android platform-tools (adb) are installed and on PATH for the session.
- Build the debug APK with Gradle.
- Install on a device and reproduce the issue.
- Capture filtered Room+app logs and paste them into a file for analysis.

1) Make adb available in this PowerShell session

If you installed Android SDK via Android Studio, platform-tools are usually at:
%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools

Temporary for this session (replace if your SDK location differs):

```powershell
$env:PATH = "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools;$env:PATH"
adb devices
```

- Expected: you see your device id and "device".
- If it shows "unauthorized", accept the USB debugging prompt on the phone.

2) (Optional) Clear logcat buffer to reduce noise

```powershell
adb logcat -c
```

3) Reproduce the issue on the device

- Launch the app on the device/emulator and reproduce the migration/crash or the bug you're debugging.

4) Capture Room + relevant app logs to a file (recommended)

```powershell
# create a folder for logs first (one-time)
New-Item -ItemType Directory -Force -Path C:\temp

# dump logs filtered to Room and relevant tags to a file
adb logcat -v threadtime Room:V NotifHelper:V CountdownTimerManager:V TimerService:V *:S -d > C:\temp\room_log.txt

# Open the file to inspect/copy-paste
notepad C:\temp\room_log.txt
```

- Copy the IllegalStateException or crash stack trace plus ~20 lines before/after and paste here for analysis.

5) Build the APK (set JAVA_HOME first)

A) Try Android Studio embedded JDK (fast, no install):

```powershell
# check whether Android Studio JBR exists
Test-Path "$env:ProgramFiles\Android\Android Studio\jbr\bin\java.exe"

# if true - set it for the session (example)
$env:JAVA_HOME = "$env:ProgramFiles\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

B) If no embedded JDK, install a JDK (Temurin 17 LTS recommended):

```powershell
# using winget (Windows 10/11)
winget install --id EclipseAdoptium.Temurin.17 -e

# then find java on PATH
where.exe java
java -version

# set JAVA_HOME for this session to the directory that contains bin\java.exe (example)
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.x.x'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

C) Once java -version shows a valid JDK, build the debug APK:

```powershell
cd 'C:\Users\olale\AndroidStudioProjects\Str3ky'
.\gradlew.bat clean assembleDebug --no-daemon
```

6) Install the APK on device

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Or uninstall then reinstall (wipes DB):

```powershell
adb uninstall com.example.str3ky
adb install app\build\outputs\apk\debug\app-debug.apk
```

7) Launch app from adb (optional)

```powershell
adb shell am start -n "com.example.str3ky/com.example.str3ky.MainActivity"
```

8) Run unit tests locally

```powershell
cd 'C:\Users\olale\AndroidStudioProjects\Str3ky'
.\gradlew.bat test --no-daemon
```

Room migration notes & quick dev fallback

- We added a Room migration 1->2 that adds `current_streak` and `last_completed_date` columns to `user_table`. If the device still runs an older APK that created DB v1, you must install a build that contains the migration or otherwise Room will throw the IllegalStateException you observed.

- Safe option: keep migration in the code and install the APK built after the migration was added (recommended). This preserves data.

- Fast dev fallback (destructive — erases DB): if you only need to iterate quickly, you can temporarily allow destructive migration so Room recreates DB instead of failing. To enable (dev-only), change the DB builder in `AppModule.provideGoalDatabase(...)` to:

```kotlin
Room.databaseBuilder(app.applicationContext, GoalDatabase::class.java, "user_goals_database")
    .fallbackToDestructiveMigration()
    .build()
```

Then rebuild & reinstall. CAUTION: this will drop and recreate the DB (lose local data).

How to revert the destructive fallback

- Remove `.fallbackToDestructiveMigration()` and instead use `.addMigrations(...)` with your migration objects. Rebuild and reinstall (or restore backup DB if you have one).

Extra troubleshooting tips

- If `adb devices` returns nothing: ensure USB debugging is enabled on phone, use a data cable, accept debugging prompt.
- If Gradle complains "JAVA_HOME is not set": ensure you set `JAVA_HOME` as shown above and re-open PowerShell or export it in the current session.
- If you see `Error: could not open ... jvm.cfg` when pointing to Android Studio jbr, that that particular path is not a full JDK; try installing Temurin or use Android Studio's Gradle JDK configured in the IDE.

If you'd like I can commit this README to the repo (small PR). Say "create README" and I will add it to the repo now. If you want the destructive fallback applied instead, say "apply destructive fallback" and I'll patch `AppModule.provideGoalDatabase` (dev-only change).

