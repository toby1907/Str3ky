# Alarm, Timer, and Achievements TODO

1. **Alarm reminders**
   - [ ] Audit `GoalRepositoryImpl.scheduleRemindersForGoal` for exact/idle scheduling gaps and add boot-complete rescheduling.
   - [ ] Ensure `AlarmReceiver` deep link to `myapp://progressscreen` populates `goalId`/`progressDate` and requests POST_NOTIFICATIONS when needed.
   - [ ] Add user UX for exact alarm permission (Android 12+) and fallbacks for devices without exact permission.

2. **Timer foreground service & notifications**
   - [ ] Refactor `TimerService` to handle explicit START/PAUSE/RESUME/STOP intents and keep a single coroutine scope tied to lifecycle.
   - [ ] Persist timer state (`timeLeft`, `phase`, `goalId`, `session args`) via DataStore/Room so service can recreate after process death.
   - [ ] Harden `DefaultNotificationHelper` so PendingIntent request codes are unique per timer, notifications share one channel, and actions (pause/resume) reflect actual state.

3. **Countdown persistence & UX**
   - [ ] Update `CountdownTimerManager` to restore countdown from persisted snapshot after app relaunch/background kill.
   - [ ] Reconcile notification actions with in-app pause/resume buttons to avoid duplicate starts.
   - [ ] Add unit tests covering pause/resume/complete flows and WorkManager hand-off for long timers.

4. **Completion & achievements**
   - [ ] Ensure `showTimerCompletedNotification` deep link to `myapp://donescreen` populates UI with the just-finished session.
   - [ ] On completion, call `checkAndUnlockAchievements`, persist unlocked achievements, and trigger optional notification linking to `ACHIEVEMENTS_SCREEN`.
   - [ ] Surface achievement unlock banners on `CompletedScreen` and provide CTA to achievements list.

5. **Permissions & onboarding**
   - [ ] Add runtime POST_NOTIFICATIONS flow with rationale + settings link.
   - [ ] Document alarm/notification behavior in-app (e.g., settings screen toggle) so users can re-enable if disabled.

















