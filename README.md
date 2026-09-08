# SecureDroid — Real-Time Privacy Monitoring and Permission Analysis System

SecureDroid is an Android-based privacy monitoring, risk analysis, and permission advisor application connected to a production Python FastAPI backend and MySQL database.

---

## 1. Project Overview

The system helps Android users understand and mitigate privacy risks associated with installed applications, requested permissions, and privacy policies. 

### Key Features
1. **Application Permission Analysis**: Inspects installed device apps, parses requested permissions, and categorizes sensitivities.
2. **Transparent Privacy Risk Scoring**: Calculates an explainable 0–100 risk score and categorizes risk into **LOW**, **MEDIUM**, **HIGH**, or **CRITICAL**.
3. **AI Privacy Policy Evaluation**: Connects to Google Gemini API to analyze raw Privacy Policy text for data collection practices, location tracking, and third-party sharing.
4. **Real-Time Privacy Monitoring**: Monitors privacy events on Android and triggers system notifications for high-risk access.
5. **Privacy Audit History**: Maintains a persistent chronological log of scans, scores, and events.
6. **Actionable Recommendations**: Generates tailored advice for revoking or restricting sensitive access in device settings.
7. **Production API & MySQL**: Clean FastAPI architecture with JWT authentication, SQLAlchemy ORM, Alembic migrations, Docker support, and Pytest coverage.

---

## 2. System Architecture

```
+-------------------------------------------------------------+
|                     Android Frontend                        |
|   (Splash, Auth, Dashboard, App Inspector, History, AI)     |
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

## 3. Technology Stack

### Android Frontend
* **IDE**: Android Studio
* **Language**: Java
* **UI**: Material Design 3, XML Layouts, Custom Gradient Aesthetics
* **Networking**: Retrofit 2, OkHttp 3, Gson
* **Storage**: `SharedPreferences` SessionManager

### FastAPI Backend
* **Language**: Python 3.11+
* **Framework**: FastAPI, Uvicorn
* **ORM**: SQLAlchemy 2.0
* **Database**: MySQL / PyMySQL
* **Migrations**: Alembic
* **Authentication**: PyJWT, Bcrypt
* **AI Integration**: Google GenAI SDK (`gemini-2.5-flash`)
* **Testing**: Pytest, TestClient

---

## 4. Folder Structure

```text
SecureDroid/
│
├── backend/
│   ├── app/
│   │   ├── main.py
│   │   ├── core/ (config.py, security.py, database.py)
│   │   ├── models/ (user.py, application.py, permission.py, analysis.py, privacy_event.py, recommendation.py, history.py)
│   │   ├── schemas/ (auth.py, user.py, application.py, permission.py, analysis.py, privacy_event.py, recommendation.py, dashboard.py, history.py)
│   │   ├── services/ (auth_service.py, permission_service.py, risk_service.py, recommendation_service.py, ai_service.py)
│   │   └── routers/ (auth.py, users.py, applications.py, permissions.py, analysis.py, monitoring.py, history.py, dashboard.py)
│   ├── alembic/
│   ├── tests/ (test_auth.py, test_risk_engine.py, test_applications.py, test_security.py)
│   ├── requirements.txt
│   ├── .env.example
│   ├── Dockerfile
│   └── docker-compose.yml
│
└── SecureDroid/ (Android Studio Project)
    └── app/src/main/
        ├── java/com/example/securedroid/
        │   ├── activities/ (auth, dashboard, AppDetails, PrivacyHistory, etc.)
        │   ├── fragments/ (Home, Apps, Alerts, Website, Profile)
        │   ├── api/ (ApiClient, AuthInterceptor, Retrofit interfaces, DTOs)
        │   ├── models/ (Data models)
        │   ├── services/ (PrivacyMonitorService)
        │   ├── notification/ (NotificationHelper)
        │   └── utils/ (SessionManager, PackageManagerHelper)
        └── res/ (layout, drawable, color, values)
```

---

## 5. MySQL Database Schema

The database consists of 8 interconnected MySQL tables:

1. `users`: User accounts, emails, password hashes.
2. `applications`: Catalog of scanned Android app packages.
3. `permissions`: Standard Android permissions with categorization, risk levels, and descriptions.
4. `application_permissions`: Many-to-many relationship linking apps to requested permissions.
5. `privacy_analyses`: Calculated risk scores, levels, and summaries per user analysis.
6. `privacy_events`: Detected real-time monitoring events.
7. `recommendations`: Actionable recommendations generated for an analysis.
8. `privacy_history`: Persistent user privacy activity timeline log.

---

## 6. Risk Scoring Algorithm

The transparent risk scoring engine calculates a 0–100 score based on permission weights and combination bonuses:

$$\text{RiskScore} = \min\left(100, \sum \text{Weight}(\text{Permission}) + \text{CombinationBonus}\right)$$

### Weights
* **CRITICAL** (SMS, System Overlay): 35 points
* **HIGH** (Camera, Microphone, GPS Location, Contacts, Call Log): 20 points
* **MEDIUM** (Bluetooth, WiFi State, App Visibility): 8 points
* **LOW** (Internet, Vibrate, Network State): 2 points

### Combination Multipliers
* **Camera + Microphone**: +15 points
* **GPS Location + $\ge 3$ Sensitive Permissions**: +10 points
* **SMS + Contacts**: +15 points

### Categories
* `0 – 30`: **LOW**
* `31 – 60`: **MEDIUM**
* `61 – 80`: **HIGH**
* `81 – 100`: **CRITICAL**

---

## 7. Setup & Run Guide

### Option A: Local Run (FastAPI + MySQL/SQLite)

1. **Install Backend Dependencies**:
   ```bash
   cd backend
   pip install -r requirements.txt
   ```

2. **Configure Environment Variables**:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   Set `DATABASE_URL` (MySQL or fallback SQLite) and `GEMINI_API_KEY`.

3. **Run Database Migrations**:
   ```bash
   alembic upgrade head
   ```

4. **Start FastAPI Backend**:
   ```bash
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```
   * Interactive Docs: `http://127.0.0.1:8000/docs`
   * OpenAPI JSON: `http://127.0.0.1:8000/api/openapi.json`

5. **Run Pytest Suite**:
   ```bash
   python -m pytest tests/ -v
   ```

### Option B: Docker Setup

Run backend and MySQL in Docker containers:
```bash
cd backend
docker-compose up --build -d
```

### Option C: Android Setup

1. Open `SecureDroid/` directory in **Android Studio**.
2. Sync Gradle files.
3. Launch an Android Emulator. (The app communicates with dev backend via `http://10.0.2.2:8000/api/`).
4. Build and Run on Emulator or physical device.

---

## 8. API Endpoint Inventory

| Category | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/register` | Register new user account |
| **Auth** | `POST` | `/api/auth/login` | Authenticate user and return JWT |
| **Auth** | `GET` | `/api/auth/me` | Fetch current user profile |
| **User** | `GET` | `/api/users/me` | Read authenticated user details |
| **User** | `PUT` | `/api/users/me` | Update name, mobile, or password |
| **User** | `DELETE` | `/api/users/me` | Delete account and user data |
| **App** | `POST` | `/api/applications` | Register/sync application package |
| **App** | `GET` | `/api/applications` | List scanned applications |
| **App** | `GET` | `/api/applications/{id}` | Get application details |
| **Permission**| `GET` | `/api/permissions` | List permission catalog |
| **Analysis** | `POST` | `/api/analysis/application` | Perform app permission risk analysis |
| **Analysis** | `POST` | `/api/analysis/privacy-policy` | Perform AI Privacy Policy analysis |
| **Dashboard**| `GET` | `/api/dashboard` | Fetch unified security metrics |
| **Monitor** | `POST` | `/api/monitoring/events` | Record background monitoring event |
| **Monitor** | `GET` | `/api/monitoring/events` | List privacy alert logs |
| **History** | `GET` | `/api/history` | List user privacy audit history |

---

## 9. Android Technical Limitations Notice

SecureDroid adheres strictly to standard Android security boundaries and user-granted permission models.

* SecureDroid **does NOT** intercept third-party application encrypted HTTPS traffic.
* SecureDroid **does NOT** read third-party private sandbox files.
* SecureDroid analyzes permissions, queries device `PackageManager`, monitors system events legally permitted by Android, and provides explainable security recommendations.
