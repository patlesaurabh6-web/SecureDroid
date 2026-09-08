# Implementation Plan - Data Models Creation

Create a comprehensive set of data models (POJOs) to support the security and privacy features of the SecureDroid application. These models will be used for data handling in the dashboard, alerts, application lists, and security analysis.

## User Review Required

> [!NOTE]
> The models are designed as standard Java POJOs with private fields, constructors, and getters. If you prefer using libraries like Lombok or specific serialization annotations (e.g., Gson's `@SerializedName`), please let me know.

## Proposed Changes

### [Component Name] Data Models
All files will be created in `app/src/main/java/com/example/securedroid/models/`.

#### [NEW] [UserModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/UserModel.java)
- Fields: `id`, `name`, `email`, `profileImage`, `joiningDate`.

#### [NEW] [AlertModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/AlertModel.java)
- Fields: `id`, `title`, `description`, `timestamp`, `severity` (enum/string), `isRead`.

#### [NEW] [TimelineModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/TimelineModel.java)
- Fields: `id`, `title`, `description`, `timestamp`, `iconRes`.

#### [NEW] [StatisticModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/StatisticModel.java)
- Fields: `label`, `value`, `iconRes`.

#### [NEW] [RecommendationModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/RecommendationModel.java)
- Fields: `id`, `title`, `description`, `actionText`, `type`.

#### [NEW] [AppModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/AppModel.java)
- Fields: `packageName`, `appName`, `version`, `iconRes`, `riskLevel`, `isSystemApp`.

#### [NEW] [WebsiteModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/WebsiteModel.java)
- Fields: `url`, `safetyStatus`, `lastChecked`, `threatDetails`.

#### [NEW] [PermissionModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/PermissionModel.java)
- Fields: `name`, `description`, `isDangerous`, `isGranted`.

#### [NEW] [RiskScoreModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/RiskScoreModel.java)
- Fields: `score`, `level`, `lastAnalysisDate`.

#### [NEW] [NotificationModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/NotificationModel.java)
- Fields: `id`, `title`, `message`, `timestamp`, `type`.

#### [NEW] [HistoryModel.java](file:///C:/Users/ACER/AndroidStudioProjects/SecureDroid/app/src/main/java/com/example/securedroid/models/HistoryModel.java)
- Fields: `id`, `action`, `details`, `timestamp`.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure all model files compile correctly within the project.

### Manual Verification
- Verify that the models can be instantiated in `DashboardActivity` or other activities as needed.
