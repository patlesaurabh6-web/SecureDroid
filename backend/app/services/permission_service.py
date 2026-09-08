from typing import Dict, Any

# Map of standard Android permissions to their categorization, risk level, sensitivity, and description
PERMISSION_DATABASE: Dict[str, Dict[str, Any]] = {
    # High / Critical Risk Permissions
    "android.permission.CAMERA": {
        "category": "Hardware / Camera",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to capture photos and record videos using device camera."
    },
    "android.permission.RECORD_AUDIO": {
        "category": "Hardware / Microphone",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to access device microphone and record ambient audio."
    },
    "android.permission.ACCESS_FINE_LOCATION": {
        "category": "Location",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to obtain precise device GPS location coordinates."
    },
    "android.permission.ACCESS_COARSE_LOCATION": {
        "category": "Location",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to estimate device location using network/cell tower data."
    },
    "android.permission.READ_CONTACTS": {
        "category": "Personal Data / Contacts",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to read user contact list and personal address book."
    },
    "android.permission.WRITE_CONTACTS": {
        "category": "Personal Data / Contacts",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to modify or delete device contacts."
    },
    "android.permission.READ_CALL_LOG": {
        "category": "Telephony / Call Log",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to read detailed incoming and outgoing call records."
    },
    "android.permission.READ_SMS": {
        "category": "Messages / SMS",
        "risk_level": "CRITICAL",
        "sensitive": True,
        "description": "Allows application to read SMS text messages, including 2FA verification codes."
    },
    "android.permission.SEND_SMS": {
        "category": "Messages / SMS",
        "risk_level": "CRITICAL",
        "sensitive": True,
        "description": "Allows application to send SMS text messages without user intervention."
    },
    "android.permission.READ_PHONE_STATE": {
        "category": "Telephony / Phone Identifier",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to read device phone number, IMEI/network info, and call state."
    },
    "android.permission.READ_MEDIA_IMAGES": {
        "category": "Storage / Media",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to access stored images and photos on device."
    },
    "android.permission.READ_MEDIA_VIDEO": {
        "category": "Storage / Media",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to access stored video files on device."
    },
    "android.permission.READ_EXTERNAL_STORAGE": {
        "category": "Storage",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to read files on external storage."
    },
    "android.permission.WRITE_EXTERNAL_STORAGE": {
        "category": "Storage",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to modify or delete files on external storage."
    },
    "android.permission.SYSTEM_ALERT_WINDOW": {
        "category": "System / Overlay",
        "risk_level": "CRITICAL",
        "sensitive": True,
        "description": "Allows application to draw overlay windows on top of other apps (potential screen spying/overlay attacks)."
    },
    # Medium Risk Permissions
    "android.permission.BLUETOOTH": {
        "category": "Connectivity",
        "risk_level": "MEDIUM",
        "sensitive": False,
        "description": "Allows application to connect to paired Bluetooth devices."
    },
    "android.permission.ACCESS_WIFI_STATE": {
        "category": "Network",
        "risk_level": "MEDIUM",
        "sensitive": False,
        "description": "Allows application to view information about Wi-Fi state."
    },
    "android.permission.QUERY_ALL_PACKAGES": {
        "category": "App Visibility",
        "risk_level": "MEDIUM",
        "sensitive": True,
        "description": "Allows application to view all installed applications on device."
    },
    "android.permission.PACKAGE_USAGE_STATS": {
        "category": "App Usage",
        "risk_level": "HIGH",
        "sensitive": True,
        "description": "Allows application to monitor component and application usage stats."
    },
    # Low Risk Permissions
    "android.permission.INTERNET": {
        "category": "Network",
        "risk_level": "LOW",
        "sensitive": False,
        "description": "Allows application to create network sockets and open internet connections."
    },
    "android.permission.ACCESS_NETWORK_STATE": {
        "category": "Network",
        "risk_level": "LOW",
        "sensitive": False,
        "description": "Allows application to access information about network connectivity."
    },
    "android.permission.VIBRATE": {
        "category": "Hardware / Haptics",
        "risk_level": "LOW",
        "sensitive": False,
        "description": "Allows application to control device haptic vibrator."
    },
    "android.permission.RECEIVE_BOOT_COMPLETED": {
        "category": "System",
        "risk_level": "LOW",
        "sensitive": False,
        "description": "Allows application to start running as soon as system boot completes."
    }
}

def classify_permission(perm_name: str) -> Dict[str, Any]:
    """Returns permission metadata. Fallback to generic low/medium if unknown."""
    if perm_name in PERMISSION_DATABASE:
        return PERMISSION_DATABASE[perm_name]
    
    # Generic normalization if short name provided
    for key, data in PERMISSION_DATABASE.items():
        if key.endswith(perm_name.upper()):
            return data

    # Unknown permission default
    is_sensitive = any(kw in perm_name.upper() for kw in ["CAMERA", "LOCATION", "AUDIO", "CONTACT", "SMS", "PHONE", "STORAGE"])
    return {
        "category": "Other",
        "risk_level": "HIGH" if is_sensitive else "LOW",
        "sensitive": is_sensitive,
        "description": f"Custom or platform permission: {perm_name}"
    }
