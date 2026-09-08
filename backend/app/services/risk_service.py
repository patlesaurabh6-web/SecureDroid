from typing import List, Dict, Any, Tuple
from app.services.permission_service import classify_permission

# Scoring weights per risk level
WEIGHT_MAP = {
    "LOW": 2,
    "MEDIUM": 8,
    "HIGH": 20,
    "CRITICAL": 35
}

def calculate_privacy_risk(permissions: List[str]) -> Tuple[float, str, List[str]]:
    """
    Calculates a transparent, explainable privacy risk score (0-100),
    risk level category, and explainable breakdown reasons.
    """
    total_score = 0.0
    reasons = []
    
    classified_perms = [classify_permission(p) for p in permissions]
    
    high_sensitive_count = 0
    has_camera = False
    has_mic = False
    has_location = False
    has_contacts = False
    has_sms = False

    for perm_name, meta in zip(permissions, classified_perms):
        level = meta["risk_level"]
        weight = WEIGHT_MAP.get(level, 2)
        total_score += weight

        if meta["sensitive"]:
            high_sensitive_count += 1

        # Track key sensitive combinations
        upper = perm_name.upper()
        if "CAMERA" in upper:
            has_camera = True
            reasons.append("Camera access detected (Potential visual privacy risk)")
        elif "RECORD_AUDIO" in upper or "MICROPHONE" in upper:
            has_mic = True
            reasons.append("Microphone access detected (Potential audio recording risk)")
        elif "LOCATION" in upper:
            has_location = True
            reasons.append("Location access detected (Potential geolocation tracking risk)")
        elif "CONTACT" in upper:
            has_contacts = True
            reasons.append("Contacts access detected (Potential address book exposure)")
        elif "SMS" in upper:
            has_sms = True
            reasons.append("SMS access detected (Potential message/2FA code exposure)")

    # Combination multipliers
    if has_camera and has_mic:
        total_score += 15
        reasons.append("Combination risk: Simultaneous Camera & Microphone access detected")
    
    if has_location and high_sensitive_count >= 3:
        total_score += 10
        reasons.append("Combination risk: Location access combined with multiple sensitive permissions")

    if has_sms and has_contacts:
        total_score += 15
        reasons.append("Combination risk: SMS and Contacts permissions combined")

    # Cap score at 100 max
    score = min(100.0, max(0.0, round(total_score, 1)))

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
