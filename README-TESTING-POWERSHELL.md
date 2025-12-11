# Testing and Building from PowerShell (Windows)

This guide shows the exact PowerShell commands I use to build the app, run tests, and capture logs for debugging outside of Android Studio.

> NOTE: You must have a full JDK installed (Temurin/Adoptium, Azul, Oracle, or other) and Android SDK platform-tools installed.

Quick checklist before you start
- Install JDK 17 (or a JDK compatible with your Gradle & compile options).
- Install Android SDK and ensure platform-tools (adb) are installed.
- Optionally install Android Studio (recommended) for faster dev iteration.

1) Verify `adb` is available (platform-tools)
```powershell
# Add platform-tools temporarily for this shell session (adjust path if needed)
$env:PATH = "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools;$env:PATH"
adb devices
```
If you see your device listed, adb is available.

2) Ensure `java` (JDK) is available
```powershell
# Set JAVA_HOME for this shell session. Replace with your actual JDK path.
$env:JAVA_HOME = 'C:\Program Files\Amazon Corretto\jdk17'  # example
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```
If you get `Error: could not open '...jvm.cfg'` then use a standard JDK installation (not Android Studio's JBR) for CLI builds.

3) Build the app (clean + assembleDebug)
```powershell
cd 'C:\Users\olale\AndroidStudioProjects\Str3ky'
.\gradlew.bat clean assembleDebug --no-daemon --stacktrace | Tee-Object -FilePath build-output.txt
```
- This runs a clean build and saves the build log to `build-output.txt`. If `kapt` fails, paste the relevant sections of the log here (use the last ~200 lines). If you prefer smaller output, try the single task:
```powershell
.\gradlew.bat :app:kaptDebugKotlin --no-daemon --stacktrace | Tee-Object -FilePath kapt-output.txt
```

4) Run unit tests
```powershell
.\gradlew.bat test --no-daemon --stacktrace | Tee-Object -FilePath test-output.txt
```

5) Capture runtime logs from a device (adb logcat)
```powershell
# Show verbose thread-time output, filter Room logs (example):
adb logcat -v threadtime Room:V *:S
# For all logs (bigger):
adb logcat -v threadtime | Tee-Object -FilePath device-log.txt
```

6) Common fixes if kapt/build fails
- Make sure `kapt` plugin is applied in `app/build.gradle.kts` (it is in this repo).
- Ensure Room/Hilt compiler dependencies are declared with `kapt(...)` in `app/build.gradle.kts`.
- If you see `incompatible types: Object cannot be converted to Annotation` in kapt stubs, that usually means an annotation type (custom qualifier) could not be resolved during kapt. Ensure the qualifier file exists and is in the same package/imported where used.
- If you see Hilt missing binding errors, check that either:
  - the implementation has an `@Inject` constructor, or
  - a `@Module` provides/binds the type. Example: `AppModule` in `app/src/main/java/.../di` contains several `@Provides` methods.

7) If you want me to continue
- Run one of the build commands above and paste the output (the last ~200 lines if large). I'll parse errors and fix the source files in your repository. If kapt is the error, copy the KAPT section or `kapt-output.txt` and paste it here.

Security note: don't paste long unrelated logs that may contain secrets; the build logs in this repository typically contain only compiler diagnostics.

-- End of guide --

