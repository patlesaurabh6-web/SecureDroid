from typing import List, Dict, Any

def generate_recommendations(permissions: List[str], risk_level: str) -> List[Dict[str, str]]:
    """
    Generates actionable privacy recommendations based on detected permissions and overall risk level.
    """
    recommendations = []
    
    perm_set = {p.upper() for p in permissions}

    if any("CAMERA" in p for p in perm_set):
        recommendations.append({
            "title": "Review Camera Access",
            "description": "Ensure this application genuinely requires camera access. If not needed continuously, revoke camera permission in Android system settings.",
            "priority": "HIGH"
        })

    if any("MICROPHONE" in p or "RECORD_AUDIO" in p for p in perm_set):
        recommendations.append({
            "title": "Limit Audio Recording Permission",
            "description": "Check if microphone access is only enabled 'While using the app'. Avoid setting 'Allow all the time'.",
            "priority": "HIGH"
        })

    if any("LOCATION" in p for p in perm_set):
        recommendations.append({
            "title": "Use Approximate Location",
            "description": "If fine GPS accuracy is not required, downgrade permission to 'Coarse/Approximate Location' in device settings.",
            "priority": "MEDIUM"
        })

    if any("CONTACT" in p for p in perm_set):
        recommendations.append({
            "title": "Protect Address Book",
            "description": "Verify why the app needs your contact list. Consider revoking contact access if social syncing is not used.",
            "priority": "HIGH"
        })

    if any("SMS" in p for p in perm_set):
        recommendations.append({
            "title": "Audit SMS Permissions",
            "description": "SMS permissions allow reading verification codes. Only grant to trusted default SMS handler applications.",
            "priority": "CRITICAL"
        })

    if risk_level in ["HIGH", "CRITICAL"] and not recommendations:
        recommendations.append({
            "title": "Perform Full Permission Audit",
            "description": "This app requests multiple sensitive permissions. Open Settings > Apps > Permissions to review granted access.",
            "priority": risk_level
        })

    if not recommendations:
        recommendations.append({
            "title": "Maintain Regular Scans",
            "description": "Your app permissions appear normal. Continue monitoring when app updates occur.",
            "priority": "LOW"
        })

    return recommendations
