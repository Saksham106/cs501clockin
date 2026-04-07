## ClockIn MVP (CS501) — Slide Content

---

## 1) Title

**ClockIn**: A mobile-first time tracking app (Compose + Room + Location + Retrofit)

- Goal: help users track what they **actually** did (not just planned)
- MVP focus: fast session tracking + history + edit + lightweight reflection

---

## 2) Problem & Motivation

- Students/professionals often *plan* time, but don’t know where it actually went
- Tracking must be **fast** and **interruption-friendly**
- Mobile is ideal for quick “start/stop” interactions throughout the day

---

## 3) What the app does (high level)

- Pick a **tag** (Study / Class / Gym / Work / Errands)
- **Start** a session, then **End** it
- View sessions in **History**
- Tap a session to **Edit** (tag, notes, start/end time) or **Delete**
- See **Dashboard** totals for today (time per tag)
- Optional: fetch **weather** for your current location (Open‑Meteo)

---

## 4) Live demo script (60–90 seconds)

1. Home → select **Study** → Start Session → End Session
2. History → show newly saved session (persists after restart)
3. Tap session → Edit tag/notes + adjust start/end → Save
4. Dashboard → show today totals by tag
5. Home → enable location permission → show lat/lon + current temperature

---

## 5) Screens (Compose UI)

- **Home**: quick tag chips + start/end + location + weather
- **History**: `LazyColumn` list of sessions; tap to edit
- **Edit Session**: form + validation + delete confirmation dialog
- **Dashboard**: totals-by-tag for today
- **Settings**: MVP placeholder

Material 3 + `Scaffold` + responsive layouts + empty/loading/error states.

---

## 6) State, ViewModel, and Architecture (MVVM-ish)

**UI layer (Compose)** is mostly stateless: screens receive state + callbacks.

**ViewModels (state holders)** expose `StateFlow` and run work in `viewModelScope`:
- `HomeViewModel`: active session + tag selection; persists completed session
- `HistoryViewModel`: observes Room sessions
- `EditSessionViewModel`: loads by `sessionId`, saves/deletes
- `LocationViewModel`: permission-aware location fetch
- `WeatherViewModel`: Open‑Meteo fetch + loading/error state

**Data layer**
- `SessionRepository` is the single source of truth for sessions (Room-backed)

---

## 7) Navigation & app flow

Navigation Compose routes:
- Home, History, Dashboard, Settings (bottom navigation)
- Edit screen uses an argument: `edit/{sessionId}`

Flow:
- History item click → navigate to Edit with `sessionId` → save/delete → back

---

## 8) Data persistence (Room)

Room schema:
- `sessions` table (`SessionEntity`)
  - id, tag, startTimeMillis, endTimeMillis, notes, edited

DAO:
- observe all sessions (Flow)
- observe session by ID (Flow)
- upsert + delete

Result:
- Sessions **persist across app restarts** and drive History/Dashboard.

---

## 9) Location integration

- Requests runtime permission: **ACCESS_FINE_LOCATION**
- Uses fused location provider to read last known location
- Displays lat/lon on Home
- Drives weather fetch (ties location to a visible feature)

---

## 10) API integration (Retrofit + coroutines)

API:
- Open‑Meteo: `GET /v1/forecast?latitude=…&longitude=…&current=temperature_2m`

Implementation:
- Retrofit + Moshi converter
- `WeatherViewModel.refresh()` uses `viewModelScope.launch`
- UI shows: loading → temperature → error (if offline/denied)

Note: requires `android.permission.INTERNET`.

---

## 11) “Sensor” requirement note (course rubric)

- In many rubrics, **Location/Maps** and **Sensors** are graded separately.
- This MVP demonstrates **Location** clearly (permission + location logic + UI + API tie-in).
- If a separate **SensorManager** sensor is required, it can be added (accelerometer/step counter) as a small additional card.

---

## 12) UX / Mobile thinking

- Fast “micro-interactions”: tag chips + big start button
- Clear status (Idle vs Running)
- Interruption handling: sessions persist and can be edited later
- Feedback states:
  - empty history message
  - weather loading/error
  - delete confirmation dialog
- Home is scrollable on small screens

---

## 13) Challenges & fixes

- ViewModel visibility/reflection issues fixed during development
- Retrofit/Moshi converter setup required codegen + Kotlin adapter
- Runtime permissions (location + internet) required correct manifest setup

---

## 14) Next steps (post-MVP)

- True pause/resume session + persistent notification
- DataStore preferences (notifications/location toggles, default tags)
- Better time editing UX (time picker dialogs)
- Map UI (optional)
- More analytics (week view, trends)

