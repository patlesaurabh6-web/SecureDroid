# Implementation Plan — Real-Time Privacy Monitoring & Permission Analysis System (SecureDroid)

Complete end-to-end integration and development plan for **SecureDroid**, an Android privacy monitoring application paired with a Python FastAPI backend and MySQL database.

---

## 1. Codebase Inspection & Status Report

Based on the preliminary inspection of `SecureDroid/`, here is the status of the existing codebase:

### Status Overview Table

| Status | Component | Details / Location |
| :--- | :--- | :--- |
| **COMPLETED** | UI Theme & Resources | Colors, custom drawables, typography (Poppins), animations (`activity_splash.xml`), gradient backgrounds. |
| **COMPLETED** | UI Layout Files | `activity_login.xml`, `activity_register.xml`, `activity_dashboard.xml` (with modular header, score card, stats, quick actions, recent alerts, timeline, bottom navigation). |
| **COMPLETED** | Data Models | `UserModel`, `AppModel`, `PermissionModel`, `AlertModel`, `StatisticModel`, `TimelineModel`, `RecommendationModel`, `HistoryModel`, `WebsiteModel`, `OnboardingModel`. |
| **COMPLETED** | RecyclerView Adapters | `AlertAdapter.java`, `TimelineAdapter.java`, `OnboardingAdapter.java`. |
| **PARTIALLY DONE** | Auth Screens | `LoginActivity.java` & `RegisterActivity.java` have local UI validation, but lack API integration, JWT token management, and error states. |
| **PARTIALLY DONE** | Dashboard | `DashboardActivity.java` inflates views, but has broken imports and static data rather than fetching `GET /api/dashboard`. |
| **STUBS** | App & Permission Screens | `AppDetailsActivity`, `PermissionDetailsActivity`, `WebsiteAnalysisActivity`, `PrivacyHistoryActivity`, `ProfileActivity`, `SettingsActivity` are empty activity stubs. |
| **STUBS** | Fragments | `HomeFragment`, `AppsFragment`, `AlertsFragment`, `WebsiteFragment`, `ProfileFragment` contain default template code. |
| **BROKEN** | Java Imports | `DashboardActivity.java` & `DashboardRepository.java` contain incorrect package imports (`com.yourpackage.*` instead of `com.example.securedroid.*`). |
| **BROKEN** | Manifest Permissions | `AndroidManifest.xml` lacks `INTERNET`, `ACCESS_NETWORK_STATE`, `QUERY_ALL_PACKAGES` permissions required for networking and app inspection. |
| **MISSING** | Android API Layer | `com.example.securedroid.api` is empty. Missing Retrofit client, API service interfaces, `AuthInterceptor`, and `SessionManager`. |
| **MISSING** | App Scanner & Monitor | Missing `PackageManager` inspector utility and background monitoring service (`PrivacyMonitorService`) for permission alerts. |
| **MISSING** | FastAPI Backend | Complete `backend/` directory is missing (FastAPI app, SQLAlchemy models, Pydantic schemas, routers, services, Alembic, tests). |
| **MISSING** | Database | MySQL database schema, relationships, foreign keys, indexes, and migrations missing. |

---

## 2. User Review Required

> [!IMPORTANT]
> **Backend Placement & Port**: The backend will be constructed in a `backend/` root folder alongside `SecureDroid/`. Android will connect to `http://10.0.2.2:8000/` when tested in the Android Emulator.

> [!IMPORTANT]
> **AI / Gemini Integration**: Google Gemini (`google-genai`) will be integrated into the backend as an explanation & privacy policy parsing layer. It requires a `GEMINI_API_KEY` in `backend/.env`. A deterministic risk engine fallback will run if no API key is provided.

> [!NOTE]
> **Android UI Preservation**: All existing XML layouts, custom rounded cards, dark aesthetics, colors (`#152233`, `#1E88E5`, gradient background), and typography will be strictly preserved.

---

## 3. System Architecture & Components

```
+-------------------------------------------------------------+
|                     Android Frontend                        |
|   (Splash, Auth, Dashboard, App Inspection, History, AI)    |
+------------------------------+------------------------------+
                               | REST API (Retrofit + OkHttp)
                               v
+-------------------------------------------------------------+
|                     FastAPI Backend                         |
|  +--------------------+  +--------------------+  +--------+ |
|  | Auth (JWT/Bcrypt)  |  | Risk Scoring Engine|  | Gemini | |
|  +--------------------+  +--------------------+  +--------+ |
|  | Permission Scanner |  | History & Alerts   |             |
|  +--------------------+  +--------------------+             |
+------------------------------+------------------------------+
                               | SQLAlchemy ORM
                               v
+-------------------------------------------------------------+
|                     MySQL Database                          |
| (users, apps, permissions, app_permissions, analyses,       |
|  privacy_events, recommendations, privacy_history)          |
+-------------------------------------------------------------+
```

---

## 4. Proposed Changes & Implementation Phases

### Phase 1: Backend Architecture Setup (`backend/`)
Create standard FastAPI directory structure:
```text
backend/
├── app/
│   ├── main.py
│   ├── core/ (config.py, security.py, database.py)
│   ├── models/ (user.py, application.py, permission.py, analysis.py, privacy_event.py, recommendation.py, history.py)
│   ├── schemas/ (auth.py, user.py, application.py, permission.py, analysis.py, privacy_event.py, recommendation.py)
│   ├── routers/ (auth.py, users.py, applications.py, permissions.py, analysis.py, monitoring.py, history.py, dashboard.py)
│   ├── services/ (auth_service.py, permission_service.py, risk_service.py, analysis_service.py, monitoring_service.py, recommendation_service.py, ai_service.py)
│   └── utils/ (validators.py, helpers.py)
├── tests/ (test_auth.py, test_risk.py, test_apps.py, test_security.py)
├── alembic/
├── requirements.txt
├── .env.example
├── alembic.ini
├── Dockerfile
├── docker-compose.yml
└── README.md
```

### Phase 2: Database Models & Alembic Migrations
Implement SQLAlchemy models in MySQL:
1. `users`: `id`, `name`, `email`, `mobile`, `password_hash`, `created_at`, `updated_at`.
2. `applications`: `id`, `package_name`, `application_name`, `version_name`, `version_code`, `developer`, `analysis_status`, `created_at`.
3. `permissions`: `id`, `permission_name`, `permission_category`, `description`, `risk_level`, `sensitive`.
4. `application_permissions`: `id`, `application_id`, `permission_id`, `granted`, `detected_at`.
5. `privacy_analyses`: `id`, `user_id`, `application_id`, `risk_score`, `risk_level`, `analysis_summary`, `created_at`.
6. `privacy_events`: `id`, `user_id`, `application_id`, `event_type`, `description`, `severity`, `detected_at`.
7. `recommendations`: `id`, `analysis_id`, `title`, `description`, `priority`, `created_at`.
8. `privacy_history`: `id`, `user_id`, `application_id`, `analysis_id`, `event_type`, `risk_score`, `created_at`.

### Phase 3: Risk Scoring & AI Engine
- **Transparent Formula**:
  $$\text{RiskScore} = \min\left(100, \sum \text{Weight}(\text{Permission}) + \text{CombinationBonus}\right)$$
  - High risk (Camera, Location, Audio, Contacts, SMS, Call Log, Phone State): 15-25 points each.
  - Medium risk (Bluetooth, WiFi State, Storage): 5-10 points each.
  - Low risk (Internet, Vibrate, Network State): 1-2 points each.
- **Explainable Categorization**:
  - `0–30`: LOW
  - `31–60`: MEDIUM
  - `61–80`: HIGH
  - `81–100`: CRITICAL
- **AI Policy & Technical Explanation Engine**:
  - Connects to Google Gemini API to analyze raw Privacy Policy text for data collection practices, location tracking, 3rd party sharing, and generates simplified security summaries.

### Phase 4: API Routes Implementation
- **AUTH**: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- **USERS**: `GET /api/users/me`, `PUT /api/users/me`, `DELETE /api/users/me`
- **APPLICATIONS**: `POST /api/applications`, `GET /api/applications`, `GET /api/applications/{id}`, `DELETE /api/applications/{id}`
- **PERMISSIONS**: `GET /api/permissions`, `POST /api/applications/{id}/permissions`
- **ANALYSIS**: `POST /api/analysis/application`, `POST /api/analysis/privacy-policy`, `GET /api/analysis/{id}`
- **DASHBOARD**: `GET /api/dashboard`
- **MONITORING**: `POST /api/monitoring/events`, `GET /api/monitoring/events`
- **HISTORY**: `GET /api/history`, `GET /api/history/{id}`

### Phase 5: Android Build & Code Corrections
1. **Fix Broken Code**:
   - Update `DashboardActivity.java` and `DashboardRepository.java` package imports from `com.yourpackage` to `com.example.securedroid`.
2. **Add Dependencies & Manifest Permissions**:
   - Add `retrofit`, `converter-gson`, `okhttp`, `logging-interceptor` to `build.gradle.kts`.
   - Add `INTERNET`, `ACCESS_NETWORK_STATE`, `QUERY_ALL_PACKAGES` to `AndroidManifest.xml`.
3. **Build API Communication Layer**:
   - `ApiClient.java`: Singleton Retrofit builder pointing to `http://10.0.2.2:8000/api/`.
   - `AuthInterceptor.java`: Attaches `Authorization: Bearer <jwt_token>` from `SessionManager`.
   - `SessionManager.java`: Saves JWT token, user ID, email, name into `SharedPreferences`.
   - `AuthApi`, `DashboardApi`, `ApplicationApi`, `AnalysisApi`, `MonitoringApi`, `HistoryApi`.
4. **Implement App Scanner & Privacy Monitor**:
   - `PackageManagerHelper.java`: Queries device installed packages, retrieves name, icon, granted permissions, package name.
   - `PrivacyMonitorService.java`: Detects newly installed apps or sensitive permission requests and fires local Android system notifications.
5. **Connect Android Screens**:
   - `LoginActivity` & `RegisterActivity`: Real API calls, loading indicators, error handling, session persistence.
   - `DashboardActivity`: Fetches live metrics from `/api/dashboard`, handles bottom navigation fragment transactions (`HomeFragment`, `AppsFragment`, `AlertsFragment`, `WebsiteFragment`, `ProfileFragment`).
   - `AppsFragment` & `AppDetailsActivity`: Displays list of installed apps scanned from device + backend risk scores.
   - `WebsiteFragment` & `WebsiteAnalysisActivity`: Provides URL/text input for AI privacy policy evaluation.
   - `AlertsFragment`: Displays recent privacy events fetched from `/api/monitoring/events`.
   - `ProfileFragment` & `PrivacyHistoryActivity`: User profile info, logout action, historical risk logs.

---

## 5. Verification & Testing Plan

### Automated Tests (Pytest)
Run pytest inside `backend/`:
```bash
pytest backend/tests/ -v
```
- Tests registration, login, duplicate email handling, JWT authorization.
- Tests permission classification & risk score algorithm boundaries (Low, Medium, High, Critical).
- Tests application creation, scanning payloads, and security checks.

### Manual End-to-End Verification
1. **Backend Verification**:
   - Launch FastAPI: `uvicorn app.main:app --reload`
   - Access OpenAPI docs at `http://127.0.0.1:8000/docs` and test endpoints.
2. **Android Verification**:
   - Build Android project in Android Studio.
   - Launch app on Android Emulator.
   - Perform registration & login -> Verify JWT stored in `SessionManager`.
   - Open Dashboard -> Verify live data rendered from FastAPI.
   - Perform app scan -> Verify permissions parsed, submitted to `/api/analysis/application`, and risk score displayed.
   - Run Privacy Policy text analysis -> Verify Gemini response.
   - Verify notifications and history logging.

---

## 6. Docker & Documentation
- Build Docker setup (`Dockerfile` & `docker-compose.yml` for FastAPI + MySQL).
- Generate a comprehensive, professional `README.md` with setup guides, database schemas, scoring formulas, API inventories, and troubleshooting instructions.
