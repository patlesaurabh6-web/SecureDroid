# SecureDroid — Simple & Clear Project Explanation Guide
*(Ready-to-use explanation for your Project Guide, Examiner & Viva)*

---

## 1. What is SecureDroid in Simple Words?

**SecureDroid** is a mobile security app for Android phones. 

Normally, when we install apps (like games, calculators, or social media), they ask for many sensitive permissions like **Camera, Microphone, Location, Contacts, and SMS**. Most users accept without checking.

SecureDroid does 3 main things:
1. **Scans all installed apps on your phone** and checks what permissions they are taking.
2. **Calculates a Privacy Risk Score (0 to 100)** to tell you whether an app is **Safe (Green)**, **Moderate Risk (Yellow)**, or **High Risk (Red)**.
3. **Analyzes Website Privacy Policies using AI** to summarize what data a website collects and if they sell your data to third parties.

---

## 2. What Technologies (Tech Stack) Did We Use?

| Part | Technology Used | Why Did We Use It? |
|---|---|---|
| **Frontend (Mobile App)** | **Java & Android Studio** | To build the Android mobile app with Material 3 Dark UI. |
| **API Client** | **Retrofit 2 & OkHttp 3** | To connect the Android app to the backend server and send JWT login tokens. |
| **Backend (Server)** | **Python & FastAPI** | To process data, run scoring algorithms, and handle APIs fast. |
| **AI / NLP Engine** | **Google Gemini AI & NLP** | To read and summarize long, complex website privacy policies. |
| **Database** | **MySQL (with SQLite fallback)** | To securely save users, scanned apps, risk scores, and alert history. |
| **Security** | **JWT (JSON Web Token) & Bcrypt** | To encrypt passwords and secure user login sessions. |

---

## 3. How Does Data Flow in the Project? (Step-by-Step Flow)

```
[ Android Mobile Device ]
       │
       ▼ (Step 1: Scans Installed Apps via PackageManager)
[ Extracts: App Name, Package, Requested Permissions ]
       │
       ▼ (Step 2: Sends JSON data via Retrofit HTTP API)
[ Python FastAPI Backend Server ]
       │
       ▼ (Step 3: Runs Risk Scoring & Synergistic Algorithm)
[ Calculates Risk Score: 0-100 & Risk Level ]
       │
       ▼ (Step 4: Saves records to Database)
[ MySQL Database ]
       │
       ▼ (Step 5: Sends results back to Mobile App)
[ Android Dashboard & Apps Screen: Displays Green / Yellow / Red ]
```

### Step-by-Step Explanation:
1. **User Registers / Logs In**: The user creates an account. The backend encrypts the password with `bcrypt` and gives back a secure `JWT token`.
2. **Reading Phone Data**: The app uses Android's built-in `PackageManager` to look at every app installed on the device and extract its permissions (Camera, Mic, GPS, SMS, etc.).
3. **Sending Data to Backend**: The app sends this list of apps and permissions to the FastAPI backend.
4. **Calculating Risk**: The backend runs our **Risk Calculation Algorithm** and gives each app a score.
5. **Displaying Results**: The Android app shows your overall phone privacy score on the Dashboard with dynamic colors:
   - **Score 70 to 100** ➔ **Low Risk (Safe - Green Color)**
   - **Score 40 to 69** ➔ **Medium Risk (Warning - Yellow Color)**
   - **Score Below 40** ➔ **High Risk (Danger - Red Color)**
6. **Website Privacy Policy Check**: You can type or paste any website privacy policy, and our AI will summarize it in 2 sentences and highlight dangerous clauses.

---

## 4. How Did We Collect Data from the Device?

### Does it need Root access?
**No, it does NOT require Root access.**

### How it works:
In Android, the operating system knows what permissions each app has requested in its `AndroidManifest.xml`. 
We use the standard Android API:
```java
PackageManager pm = context.getPackageManager();
List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
```
From each package, we extract:
- App Name (e.g., WhatsApp, Calculator)
- Package Name (e.g., `com.whatsapp`)
- Version Name (e.g., `2.24.1`)
- List of Permissions (e.g., `android.permission.CAMERA`, `android.permission.READ_CONTACTS`)

---

## 5. What Algorithms Did We Use?

### 1. Permission Weight Scoring Algorithm
Every permission is given a risk weight based on its danger:
- **Low Risk (Weight = 2)**: Internet, Vibrate, Network state (Normal permissions).
- **Medium Risk (Weight = 8)**: Bluetooth, Wi-Fi state.
- **High Risk (Weight = 20)**: Camera, Microphone, Precise GPS Location, Contacts, Storage.
- **Critical Risk (Weight = 35)**: Read SMS, Send SMS, Overlay on Top of Other Apps (`SYSTEM_ALERT_WINDOW`).

---

### 2. Synergistic Combination Multiplier (Dangerous Pairs)
An app with just Camera is okay. An app with just Microphone is okay. But an app that has **both Camera + Microphone** can spy on you without you knowing.

We check for dangerous combinations and add extra penalty points:
- **Camera + Microphone** ➔ Adds **+15 Extra Risk Points** *(Simultaneous audio-visual surveillance risk)*.
- **SMS + Contacts** ➔ Adds **+15 Extra Risk Points** *(Can read OTP/2FA codes and steal phone book)*.
- **Location + 3 or more sensitive permissions** ➔ Adds **+10 Extra Risk Points**.

**Final Score Formula**:
$$\text{Risk Score} = \text{Sum of Permission Weights} + \text{Combination Penalties}$$
*(Capped between 0 and 100)*.

---

### 3. AI & NLP Privacy Policy Analyzer
- **Model**: Google Gemini 2.5 Flash.
- **How it works**: We send the raw privacy policy text to the AI model with a prompt. The AI reads the legal text and extracts:
  1. What data the website collects (Location, Contacts, Browsing history).
  2. Who they share/sell the data with (Advertisers, Third parties).
  3. Security concerns and privacy recommendations.
- **Fallback**: If there is no internet/API key, our code uses keyword NLP scanning to identify keywords like "location", "third-party", "cookies", and "sell".

---

### 4. Smart Recommendation Algorithm
Based on the permissions found, the app gives specific advice:
- If **Camera** is found ➔ *"Revoke camera or set to 'Only while using the app'"*.
- If **Location** is found ➔ *"Switch from Precise GPS to Approximate Location"*.
- If **SMS** is found ➔ *"Ensure this is only granted to trusted SMS applications"*.

---

## 6. Top Viva / Guide Questions & Simple Answers

### Q1: What is the main objective of your project?
> **Answer**: "To give Android users full control and transparency over their privacy by scanning installed apps, detecting dangerous permission combinations, calculating an easy-to-understand risk score, and using AI to simplify long website privacy policies."

---

### Q2: Why is combination risk analysis better than normal permission scanning?
> **Answer**: "Because single permissions might be harmless on their own, but when combined (like SMS + Contacts or Camera + Microphone), an app can intercept two-factor authentication OTPs or record both video and audio. Our algorithm detects these dangerous combinations and increases the risk score."

---

### Q3: How is the frontend communicating with the backend?
> **Answer**: "The Android frontend uses Retrofit 2 to make RESTful HTTP API calls (`GET`, `POST`) to our Python FastAPI server. Every request includes a secure JWT Bearer Token in the headers so the backend knows which user is making the request."

---

### Q4: How is user data and password secured?
> **Answer**: "Passwords are never saved in plain text. We hash them using the `bcrypt` hashing algorithm in the backend. User authentication sessions use RFC 7519 standard JSON Web Tokens (JWT)."

---

### Q5: Can this app run on real Android phones?
> **Answer**: "Yes. It scans the real apps installed on the phone using Android's `PackageManager` API and connects over Wi-Fi to the FastAPI backend server in real-time."
