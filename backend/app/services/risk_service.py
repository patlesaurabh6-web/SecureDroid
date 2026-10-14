from typing import List, Dict, Any, Tuple, Optional
from app.services.permission_service import classify_permission

# Scoring weights per risk level
WEIGHT_MAP = {
    "LOW": 2,
    "MEDIUM": 8,
    "HIGH": 20,
    "CRITICAL": 35
}

def calculate_privacy_risk(
    permissions: List[str],
    granted_permissions: Optional[List[str]] = None,
    denied_permissions: Optional[List[str]] = None,
    normal_permissions: Optional[List[str]] = None,
    declared_permissions: Optional[List[str]] = None
) -> Tuple[float, str, List[str]]:
    """
    Calculates a transparent, explainable privacy risk score (0-100),
    risk level category, and explainable breakdown reasons.
    Evaluates ACTUALLY GRANTED sensitive permissions.
    Denied permissions do not contribute to risk or generate false access reasons.
    """
    # Authoritative active permissions:
    # If granted_permissions is explicitly provided (even if empty []), use it.
    # Otherwise fallback to permissions list.
    if granted_permissions is not None:
        active_perms = granted_permissions
    else:
        active_perms = permissions or []

    total_score = 0.0
    reasons = []
    
    classified_perms = [classify_permission(p) for p in active_perms]
    
    high_sensitive_count = 0
    has_camera = False
    has_mic = False
    has_location = False
    has_contacts = False
    has_sms = False
    has_phone = False
    has_storage = False

    for perm_name, meta in zip(active_perms, classified_perms):
        level = meta["risk_level"]
        weight = WEIGHT_MAP.get(level, 2)
        total_score += weight

        if meta.get("sensitive", False):
            high_sensitive_count += 1

        # Track key sensitive combinations and explanations ONLY for active permissions
        upper = perm_name.upper()
        if "CAMERA" in upper:
            has_camera = True
            reasons.append("Camera access granted (Visual capture capability active)")
        elif "RECORD_AUDIO" in upper or "MICROPHONE" in upper:
            has_mic = True
            reasons.append("Microphone access granted (Ambient audio recording active)")
        elif "LOCATION" in upper:
            has_location = True
            reasons.append("Location access granted (Geolocation tracking active)")
        elif "CONTACT" in upper:
            has_contacts = True
            reasons.append("Contacts access granted (Address book access active)")
        elif "SMS" in upper:
            has_sms = True
            reasons.append("SMS access granted (Text message access active)")
        elif "PHONE" in upper or "CALL_LOG" in upper:
            has_phone = True
            reasons.append("Phone/telephony access granted (Call state access active)")
        elif "STORAGE" in upper or "MEDIA" in upper:
            has_storage = True
            reasons.append("Storage/media access granted (Device file access active)")

    # Combination multipliers: ONLY for ACTUALLY GRANTED sensitive permissions
    if has_camera and has_mic:
        total_score += 15
        reasons.append("Combination risk: Simultaneous Camera & Microphone access granted")
    
    if has_location and high_sensitive_count >= 3:
        total_score += 10
        reasons.append("Combination risk: Location access combined with multiple sensitive permissions")

    if has_sms and has_contacts:
        total_score += 15
        reasons.append("Combination risk: SMS and Contacts permissions combined")

    # If no sensitive permissions are granted, provide baseline safe score
    if not active_perms or high_sensitive_count == 0:
        score = max(5.0, min(15.0, total_score))
        risk_level = "LOW"
        if not reasons:
            reasons.append("No sensitive permissions are currently granted to this application.")
        return score, risk_level, list(set(reasons))

    # Cap score at 100 max
    score = min(100.0, max(10.0, round(total_score, 1)))

    # Risk level categorization
    if score <= 30:
        risk_level = "LOW"
        if not reasons:
            reasons.append("Standard application permissions detected without sensitive data access.")
    elif score <= 60:
        risk_level = "MEDIUM"
    elif score <= 80:
        risk_level = "HIGH"
    else:
        risk_level = "CRITICAL"

    return score, risk_level, list(set(reasons))
