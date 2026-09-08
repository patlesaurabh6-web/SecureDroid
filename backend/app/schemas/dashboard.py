from pydantic import BaseModel
from typing import List, Optional
from app.schemas.privacy_event import PrivacyEventResponse
from app.schemas.analysis import AnalysisResponse

class DashboardSummary(BaseModel):
    overall_risk_score: float
    risk_level: str
    total_applications: int
    high_risk_applications: int
    medium_risk_applications: int
    low_risk_applications: int
    recent_events: List[PrivacyEventResponse] = []
    recent_analyses: List[AnalysisResponse] = []
    recommendation_title: Optional[str] = "Privacy Recommendation"
    recommendation_summary: Optional[str] = "Regularly review application permission access."
