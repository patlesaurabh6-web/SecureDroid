from app.core.database import Base
from app.models.user import User
from app.models.application import Application, ApplicationPermission
from app.models.permission import Permission
from app.models.analysis import PrivacyAnalysis
from app.models.privacy_event import PrivacyEvent
from app.models.recommendation import Recommendation
from app.models.history import PrivacyHistory

__all__ = [
    "Base",
    "User",
    "Application",
    "ApplicationPermission",
    "Permission",
    "PrivacyAnalysis",
    "PrivacyEvent",
    "Recommendation",
    "PrivacyHistory"
]
