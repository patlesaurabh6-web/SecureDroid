# SecureDroid — Complete System Architecture & End-to-End Flow Diagrams

This document provides a comprehensive technical breakdown and visual mapping of how the **SecureDroid** ecosystem operates, covering every layer from the Android Client OS to the FastAPI backend, mathematical risk engines, database persistence, and Google Gemini AI.

---

## Visual System Architecture Diagram

![SecureDroid Complete System Architecture Diagram](securedroid_architecture_diagram.jpg)

---

## 1. High-Level Multi-Tier System Architecture

The following diagram illustrates the complete 4-tier architecture of SecureDroid, including all internal subsystems, protocols, and boundaries.

```mermaid
graph TB
    subgraph TIER1["Tier 1: Android Mobile Client (FRONTEND)"]
        subgraph UI["Presentation Layer (Material 3 Dark UI)"]
            Splash["SplashActivity / LoginActivity"]
            Dash["DashboardActivity\n(Dynamic Score Ring & Metrics)"]
            AppsView["AppsFragment & AppDetailsActivity\n(Generic App Risk Inspection)"]
            MonitorView["LiveMonitorFragment\n(Background Process & Sensor State)"]
            AlertView["AlertDetailsActivity\n(Granular Real-Data Remediation)"]
            WebView["WebsiteScannerFragment\n(URL & AI Policy Scanner)"]
            ReportView["PrivacyReportActivity\n(Weekly Chart & PDF Exporter)"]
        end

        subgraph CORE["Android Core & Operating System Integration"]
            PMHelper["PackageManagerHelper\n(Zero-Root OS Package Inspection)"]
            AMHelper["ActivityManager / Process Monitor"]
            PDFGen["PdfDocument Generator"]
        end

        subgraph STORAGE["Client-Side Persistence"]
            Session["SessionManager\n(Encrypted SharedPreferences, JWT Cache)"]
        end

        subgraph CLIENT_NET["Networking Layer"]
            Retrofit["Retrofit 2 + Gson Converter"]
            OkHttp["OkHttp 3 Client\n(JWT Auth Interceptor & Logging)"]
        end
    end

    subgraph TIER2["Tier 2: Communication & Security Boundary"]
        REST["RESTful HTTP / JSON APIs\n(Bearer JWT Authorization Header)"]
        NSC["Network Security Configuration\n(Cleartext Debug Permitted & TLS 1.3)"]
    end

    subgraph TIER3["Tier 3: Backend Application Server (FASTAPI)"]
        API_GW["FastAPI Core Router (/api)"]
        
        subgraph ROUTERS["API Endpoints"]
            AuthR["/api/auth\n(Register, Login, Profile)"]
            AppsR["/api/apps\n(Batch Upload, Single App Analysis)"]
            DashR["/api/dashboard\n(Summary Metrics & Recommendations)"]
            MonR["/api/monitoring\n(Event Logging & Sensor Events)"]
            WebR["/api/website\n(URL Scraping & Security Checks)"]
        end

        subgraph SERVICES["Business Logic & Evaluation Engines"]
            AuthSvc["AuthService\n(Bcrypt Hash & JWT Manager)"]
            RiskEngine["RiskService\n(Permission Weights & Synergistic Multipliers)"]
            RecEngine["RecommendationService\n(Plain-English Remediation)"]
            AIEngine["Gemini AI Pipeline & NLP Parser"]
        end
    end

    subgraph TIER4["Tier 4: Persistence & Cloud Intelligence Layer"]
        subgraph DB["Relational Database Layer (SQLAlchemy ORM)"]
            MySQL[("Primary MySQL Database\n(3306)")]
            SQLite[("Local SQLite Database Fallback\n(privacy_monitoring.db)")]
        end

        subgraph CLOUD_AI["Cloud Generative AI"]
            Gemini["Google Gemini 2.5 Flash LLM"]
        end
    end

    %% Internal Client Connections
    UI --> CORE
    UI --> STORAGE
    UI --> CLIENT_NET
    CORE --> CLIENT_NET
    CLIENT_NET --> OkHttp
    OkHttp --> Retrofit

    %% Tier 1 to Tier 2
    Retrofit --> REST
    REST --> NSC
    NSC --> API_GW

    %% Tier 2 to Tier 3
    API_GW --> ROUTERS
    AuthR --> AuthSvc
    AppsR --> RiskEngine
    AppsR --> RecEngine
    DashR --> RiskEngine
    MonR --> DB
    WebR --> AIEngine

    %% Tier 3 to Tier 4
    AuthSvc --> DB
    RiskEngine --> DB
    AIEngine --> Gemini
    DB --> MySQL
    MySQL -.->|Connection Failure Fallback| SQLite
```

---

## 2. Core Workflow 1: User Authentication & Session Security Flow

This sequence diagram depicts user registration, login, cryptographic password hashing (`bcrypt`), JWT token generation, and automatic session persistence in the Android client.

```mermaid
sequenceDiagram
    autonumber
    actor User as Android User
    participant App as Android Client (LoginActivity)
    participant Session as SessionManager
    participant API as FastAPI (/api/auth)
    participant AuthSvc as AuthService
    participant DB as Database (MySQL / SQLite)

    User->>App: Inputs Username, Email & Password
    App->>API: POST /api/auth/login (email, password)
    API->>AuthSvc: authenticate_user(email, password)
    AuthSvc->>DB: Query User by Email
    DB-->>AuthSvc: Return User Record (hashed_password)
    
    critical Password Verification
        AuthSvc->>AuthSvc: verify_password(plain, hashed) via Bcrypt
    end

    alt Password Matches
        AuthSvc->>AuthSvc: create_access_token(user_id) via PyJWT
        AuthSvc-->>API: Return JWT Token (exp: 24h)
        API-->>App: 200 OK: { access_token, user: { id, username, email } }
        App->>Session: saveAuthToken(token), saveUser(user)
        App->>User: Navigate to DashboardActivity
    else Invalid Password
        API-->>App: 401 Unauthorized ("Incorrect credentials")
        App->>User: Display Error Toast / Shake Animation
    end
```

---

## 3. Core Workflow 2: Generic Application Permission Scanning & Risk Analysis Flow

This is the primary intelligence workflow of SecureDroid. It works **universally for any installed Android application** without requiring root privileges or hardcoded application lists.

```mermaid
flowchart TD
    Start([Launch Scan / App Inspection]) --> InitPM[Initialize Android PackageManager]
    InitPM --> GetPackages["Query pm.getInstalledPackages(GET_PERMISSIONS)"]
    
    subgraph OS_EXTRACTION["Android OS Static Extraction (Zero-Root)"]
        GetPackages --> LoopApps{Iterate Through Each Installed App}
        LoopApps --> ExtractPkg["Extract PackageName, AppName, Version, TargetSDK"]
        ExtractPkg --> ExtractDeclared["Extract requestedPermissions (Declared in APK Manifest)"]
        ExtractPkg --> CheckGranted["Check checkPermission() for each sensitive permission"]
        CheckGranted --> Segregate["Segregate Permissions into:\n1. declaredPermissions (Manifest List)\n2. grantedSensitivePermissions (Active Runtime Access)"]
        Segregate --> FetchIcon["Fetch Real App Drawable Icon: pm.getApplicationIcon()"]
    end

    OS_EXTRACTION --> PrepPayload[Prepare JSON Payload: AppModel]
    PrepPayload --> SendAPI["Send to Backend: POST /api/apps/batch or /api/apps/analyze"]

    subgraph BACKEND_RISK_ENGINE["FastAPI Multi-Factor Risk Calculation Engine"]
        SendAPI --> AssignWeights["1. Base Permission Weights:\n• Low Risk (Internet, Vibrate): 2 pts\n• Medium Risk (WiFi, BT): 8 pts\n• High Risk (Location, Camera, Contacts, Storage): 20 pts\n• Critical Risk (SMS, Overlay): 35 pts"]
        
        AssignWeights --> CheckCombos["2. Synergistic Combination Multiplier:\n• Camera + Microphone: +15 pts (Surveillance Risk)\n• SMS + Contacts: +15 pts (OTP / Identity Interception)\n• Location + >= 3 Sensitive Perms: +10 pts (Tracking Risk)"]
        
        CheckCombos --> CalcFormula["3. Raw Score Formula:\nScore = Min(100, Sum(Weights) + Sum(Penalties))"]
        
        CalcFormula --> ClassifyLevel{"4. Risk Level Classification"}
        ClassifyLevel -->|Score <= 30| LowRisk["LOW RISK (Green)"]
        ClassifyLevel -->|31 <= Score <= 60| MedRisk["MEDIUM RISK (Amber)"]
        ClassifyLevel -->|Score > 60| HighRisk["HIGH / CRITICAL RISK (Red)"]
        
        ClassifyLevel --> GenReasons["5. Generate Plain-English Explanations & Actionable Advice"]
    end

    BACKEND_RISK_ENGINE --> SaveDB["Save Analysis & Permissions to Database"]
    SaveDB --> ReturnClient["Return AnalysisResponse to Mobile App"]

    subgraph CLIENT_RENDERING["Dynamic Client UI Update"]
        ReturnClient --> CacheSession["Cache RiskScoreModel in SessionManager"]
        CacheSession --> RenderApps["Update AppsFragment:\n• Display Real Icon & Package\n• Filter by All, High, Med, Safe\n• Render Count: 'X Sensitive Allowed • Y Declared'"]
        CacheSession --> RenderDetails["AppDetailsActivity:\n• Circular Dynamic Risk Badge Ring\n• Plain-English Root Cause Bullets\n• Deep-Link: 'Open Android Settings' Button"]
    end

    RenderDetails --> End([Audit Complete])
```

---

## 4. Core Workflow 3: Dashboard Aggregation & Dynamic Score Visualizer Flow

The Dashboard aggregates device-wide intelligence, converting raw risk into a **Protection Health Score** ($100 - \text{Average Risk}$).

```mermaid
sequenceDiagram
    autonumber
    participant UI as DashboardActivity
    participant PM as PackageManagerHelper
    participant Session as SessionManager
    participant API as Backend Dashboard API (/api/dashboard/summary)
    participant Circle as Dynamic Score Ring (layoutScoreCircle)

    UI->>API: GET /api/dashboard/summary (Bearer JWT)
    
    alt Backend Available
        API-->>UI: 200 OK { overall_risk_score, risk_level, total_apps, high_count, med_count, recommendation }
        UI->>UI: privacyScore = Max(15, Min(100, 100 - overall_risk_score))
    else Backend Offline / 401 Fallback
        UI->>Session: Retrieve Cached App Risk Models
        UI->>PM: getInstalledApps(context)
        UI->>UI: Calculate Local Average: privacyScore = 100 - avgRisk (or penalty deduction)
    end

    critical UI Dynamic Theme & Threshold Application
        alt privacyScore >= 70 OR (level == "LOW RISK" and highApps == 0)
            UI->>Circle: Stroke Color: #00E676 (Vibrant Green)
            UI->>UI: Text: "LOW RISK (PROTECTED)"
            UI->>UI: Summary: "Your device appears secure. Privacy score is healthy."
        else privacyScore >= 40 OR level == "MEDIUM RISK"
            UI->>Circle: Stroke Color: #FFC107 (Vibrant Amber/Yellow)
            UI->>UI: Text: "MEDIUM RISK"
            UI->>UI: Summary: "Moderate risk detected. Audit apps with location/mic access."
        else privacyScore < 40 OR level == "HIGH RISK"
            UI->>Circle: Stroke Color: #FF3B30 (Vibrant Red)
            UI->>UI: Text: "HIGH RISK DETECTED"
            UI->>UI: Summary: "High privacy risk detected! Review sensitive app permissions."
        end
    end

    UI->>UI: Populate Stat Cards (Total, Medium, High, Safe Apps)
    UI->>UI: Bind Top Recent Alert Cards with Genuine Installed App Info
    UI->>UI: Display Dynamic AI Recommendation Banner
```

---

## 5. Core Workflow 4: Real-Time Live Monitoring & Hardware Sensor Tracking

This diagram models how background processes and hardware sensor hooks are displayed in `LiveMonitorFragment`.

```mermaid
flowchart TD
    UserNav[User Opens Live Monitor Tab] --> GetRunning[Query ActivityManager & Running App Tasks]
    GetRunning --> FilterBackground[Filter System Processes vs Background User Apps]
    
    subgraph SENSOR_AUDIT["Hardware Sensor State Detection"]
        FilterBackground --> CheckCam{Camera Service Active?}
        CheckCam -->|Yes| CamActive["Camera Active (Indicator: Red)"]
        CheckCam -->|No| CamIdle["Camera Inactive (Indicator: Green)"]

        FilterBackground --> CheckMic{Microphone AudioRecord Active?}
        CheckMic -->|Yes| MicActive["Microphone Active (Indicator: Red)"]
        CheckMic -->|No| MicIdle["Microphone Inactive (Indicator: Green)"]

        FilterBackground --> CheckGPS{Location Provider Updating?}
        CheckGPS -->|Yes| GPSActive["Location Active (Indicator: Red)"]
        CheckGPS -->|No| GPSIdle["Location Inactive (Indicator: Green)"]
    end

    subgraph PROCESS_LIST["Dynamic Background App Feed"]
        FilterBackground --> ResolveAppInfo["For Each Background Process:\n• Extract ApplicationInfo via PackageManager\n• Load Real App Icon (No Tint)\n• Fetch Running Status & Risk Level"]
        ResolveAppInfo --> RenderItem["Inflate item_live_monitor_app.xml:\n• App Logo\n• App Name\n• Status: 'Running in Background'\n• Color-Coded Risk Badge"]
    end

    SENSOR_AUDIT --> RenderScreen[Render Complete Live Monitor Fragment]
    PROCESS_LIST --> RenderScreen
```

---

## 6. Core Workflow 5: AI-Powered Website Privacy Policy Scanner Flow

This sequence demonstrates URL validation, TLS verification, HTML extraction, and Google Gemini 2.5 Flash policy synthesis.

```mermaid
sequenceDiagram
    autonumber
    actor User as Android User
    participant WebUI as WebsiteScannerFragment
    participant WebAPI as Backend (/api/website/scan)
    participant Scraper as Web Scraper & TLS Checker
    participant Gemini as Google Gemini 2.5 Flash API
    participant NLP as Local NLP Rule Fallback
    participant DB as Database (ScannedWebsites)

    User->>WebUI: Enters Target URL (e.g., https://example.com) & Taps "SCAN"
    WebUI->>WebAPI: POST /api/website/scan { url }
    
    WebAPI->>Scraper: Validate URL & Inspect TLS/SSL Certificate
    Scraper-->>WebAPI: TLS Valid, Protocol: HTTPS, Cipher: TLS 1.3
    
    WebAPI->>Scraper: Fetch HTML Document & Extract Privacy Policy Text
    Scraper-->>WebAPI: Cleaned Policy Text Content

    critical AI Privacy Policy Analysis
        alt Gemini API Key Available
            WebAPI->>Gemini: Prompt: Extract (1) Data Collected, (2) Third-Party Sharing/Sale, (3) 2-Sentence Summary
            Gemini-->>WebAPI: Structured JSON Analysis & Safety Score
        else API Unavailable / Network Offline
            WebAPI->>NLP: Scan DOM for Keywords ("third-party", "sell", "location", "cookies")
            NLP-->>WebAPI: Fallback Risk Score & Extracted Clauses
        end
    end

    WebAPI->>DB: Save Scan Record (url, score, risk_level, ai_summary)
    WebAPI-->>WebUI: 200 OK: { security_score: 98, risk_level: "Safe/Low Risk", ssl: true, ai_summary, indicators }
    
    WebUI->>WebUI: Animate Circular Score Ring (Green #00E676)
    WebUI->>WebUI: Display Observable Security Indicators (SSL Encrypted, Sandboxed)
    WebUI->>WebUI: Display AI Recommendation & Summary Card
```

---

## 7. Core Workflow 6: Alert Details Remediation & System Settings Deep-Linking Flow

When a user taps an alert card on the Dashboard or in the Notifications feed, SecureDroid opens `AlertDetailsActivity` with complete genuine metadata and system deep-links.

```mermaid
flowchart TD
    UserTap[User Taps Alert Card on Dashboard] --> CreateIntent[DashboardActivity builds Intent]
    CreateIntent --> AddExtras["Attach Intent Extras:\n• APP_NAME\n• PACKAGE_NAME\n• PERMISSION summary\n• TIME (lastUpdateTime)\n• CURR_RISK\n• DESCRIPTION (Plain-English Risk Reasons)"]
    
    AddExtras --> LaunchAlert[Launch AlertDetailsActivity]

    subgraph ALERT_INIT["AlertDetailsActivity Data Binding"]
        LaunchAlert --> BindHeader["Set Alert Title & Dynamic Risk Pill Badge\n(Color: Red / Amber / Green)"]
        LaunchAlert --> BindApp["Load Real App Icon via PackageManager\nBind App Name & Package Identifier"]
        LaunchAlert --> BindPerms["Format Granted Sensitive Permissions\n(e.g., 'Precise Location, Call Logs (+3 more)')"]
        LaunchAlert --> BindTime["Format Real Timestamp: MMM dd, yyyy • hh:mm a"]
        LaunchAlert --> BindReasons["Bind Detailed Risk Reasons & Factors"]
        LaunchAlert --> BindAI["Bind Contextual AI Recommendation Box"]
    end

    subgraph USER_REMEDIATION["Interactive Remediation Actions"]
        BindAI --> ActionChoice{User Selects Action}
        
        ActionChoice -->|Tap 'OPEN APP SETTINGS'| OpenSysSettings["Intent to Settings.ACTION_APPLICATION_DETAILS_SETTINGS\nUri: 'package:' + packageName\nNavigates Directly to Android OS App Settings"]
        
        ActionChoice -->|Tap 'VIEW FULL APP ANALYSIS'| OpenAnalysis["Intent to AppDetailsActivity\nPasses PackageName for Full Breakdown"]
        
        ActionChoice -->|Tap 'MARK AS REVIEWED'| DismissAlert["Dismiss Alert Card & Return to Dashboard"]
    end

    OpenSysSettings --> UserRevoke["User Revokes Dangerous Permission in Android OS"]
    UserRevoke --> ReturnApp["User Returns to SecureDroid"]
    ReturnApp --> AutoRefresh["SecureDroid Re-Scans Device & Updates Risk Score Instantly"]
```

---

## 8. Core Workflow 7: On-Device PDF Privacy Audit Report Generation Flow

This diagram models the on-device compilation of standard A4 PDF compliance documents in `PrivacyReportActivity`.

```mermaid
flowchart TD
    UserExport[User Taps 'EXPORT PDF' on Privacy Report] --> InitPDF["Initialize android.graphics.pdf.PdfDocument"]
    InitPDF --> CreatePage["Start A4 Page: pageInfo = PageInfo.Builder(595, 842, 1).create()"]
    
    subgraph CANVAS_DRAW["Drawing High-Resolution Vector Content"]
        CreatePage --> DrawHeader["1. Draw Document Header:\n• SecureDroid Branded Banner\n• Audit Date & Time\n• Device Model & Android Version"]
        
        DrawHeader --> DrawScore["2. Draw Overall Privacy Score Box:\n• Privacy Protection Score (0-100)\n• Risk Status (Protected / Moderate / Critical)"]
        
        DrawScore --> DrawAppsTable["3. Draw Applications Breakdown Table:\n• Total Apps Audited\n• High Risk Apps Count & Packages\n• Medium Risk Apps Count & Packages\n• Safe Apps Count"]
        
        DrawAppsTable --> DrawRecommendations["4. Draw Remediation Roadmap:\n• Bulleted Action Items for User Security"]
    end

    CANVAS_DRAW --> FinishPage["document.finishPage(page)"]
    FinishPage --> WriteFile["Save PDF to Device Public Storage (Downloads / Documents)"]
    WriteFile --> TriggerIntent["Launch Intent.ACTION_VIEW (MIME: application/pdf)"]
    TriggerIntent --> OpenViewer[PDF Opens in Default Device PDF Viewer]
```

---

## 9. Comprehensive Data Architecture (Entity-Relationship Model)

```mermaid
erDiagram
    USERS ||--o{ APPLICATIONS : "audits"
    USERS ||--o{ PRIVACY_ANALYSES : "receives"
    USERS ||--o{ PRIVACY_EVENTS : "monitors"
    USERS ||--o{ SCANNED_WEBSITES : "scans"
    APPLICATIONS ||--o{ APP_PERMISSIONS : "requests"
    PERMISSIONS ||--o{ APP_PERMISSIONS : "referenced_by"
    APPLICATIONS ||--o{ PRIVACY_ANALYSES : "evaluated_in"
    APPLICATIONS ||--o{ PRIVACY_EVENTS : "subject_of"
    PRIVACY_ANALYSES ||--o{ RECOMMENDATIONS : "generates"

    USERS {
        int id PK
        string username
        string email
        string hashed_password
        datetime created_at
        datetime updated_at
    }

    APPLICATIONS {
        int id PK
        string app_name
        string package_name
        string version_name
        int target_sdk
        boolean is_system_app
        datetime first_seen
    }

    PERMISSIONS {
        int id PK
        string permission_name
        string category
        int risk_weight
        text description
    }

    APP_PERMISSIONS {
        int id PK
        int application_id FK
        int permission_id FK
        boolean is_granted
    }

    PRIVACY_ANALYSES {
        int id PK
        int user_id FK
        int application_id FK
        float risk_score
        string risk_level
        text analysis_summary
        datetime created_at
    }

    RECOMMENDATIONS {
        int id PK
        int analysis_id FK
        string title
        text recommendation_text
        string priority
    }

    PRIVACY_EVENTS {
        int id PK
        int user_id FK
        int application_id FK
        string event_type
        text description
        string severity
        datetime detected_at
    }

    SCANNED_WEBSITES {
        int id PK
        int user_id FK
        string url
        float security_score
        string risk_level
        boolean ssl_valid
        text ai_summary
        datetime scanned_at
    }
```

---

## 10. Summary of Architectural Guarantees

| Guarantee | Architectural Mechanism | Verification |
| :--- | :--- | :--- |
| **Zero Root Requirement** | Standard Android `PackageManager` APIs in user-space sandbox | Runs on any standard production Android device |
| **Universal Generic Compatibility** | No hardcoded package names, scores, or mock dictionaries | Tested against arbitrary apps (*Naukri*, *redBus*, *Truecaller*, *Where is My Train*) |
| **Offline Resilience** | Local evaluation fallback in `SessionManager` & SQLite DB fallback | Dashboard and App Auditing function even without internet |
| **Data Confidentiality** | No personal data (photos, SMS, audio, contacts) is ever exfiltrated | Only package metadata and declared/granted permission flags are processed |
| **Direct User Empowerment** | Android system deep-links (`ACTION_APPLICATION_DETAILS_SETTINGS`) | 1-tap navigation to revoke dangerous permissions |
