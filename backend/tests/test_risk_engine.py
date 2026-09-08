import pytest
import sys
import os

sys.path.insert(0, os.path.realpath(os.path.join(os.path.dirname(__file__), "..")))

from app.services.risk_service import calculate_privacy_risk
from app.services.permission_service import classify_permission

def test_permission_classification():
    camera_meta = classify_permission("android.permission.CAMERA")
    assert camera_meta["risk_level"] == "HIGH"
    assert camera_meta["sensitive"] is True

    vibrate_meta = classify_permission("android.permission.VIBRATE")
    assert vibrate_meta["risk_level"] == "LOW"
    assert vibrate_meta["sensitive"] is False

def test_low_risk_calculation():
    perms = ["android.permission.INTERNET", "android.permission.VIBRATE"]
    score, risk_level, reasons = calculate_privacy_risk(perms)
    assert score <= 30.0
    assert risk_level == "LOW"

def test_high_risk_combination_calculation():
    perms = [
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_CONTACTS"
    ]
    score, risk_level, reasons = calculate_privacy_risk(perms)
    assert score > 60.0
    assert risk_level in ["HIGH", "CRITICAL"]
    assert len(reasons) > 0
