from pydantic import BaseModel, ConfigDict
from typing import Optional, List
from datetime import datetime
from app.schemas.recommendation import RecommendationResponse

class AppAnalysisRequest(BaseModel):
    package_name: str
    application_name: str
    version_name: Optional[str] = "1.0.0"
    developer: Optional[str] = "Unknown"
    permissions: List[str] = []

class PolicyAnalysisRequest(BaseModel):
    privacy_policy_text: str
    website_url: Optional[str] = None

class AnalysisResponse(BaseModel):
    id: int
    user_id: int
    application_id: Optional[int]
    risk_score: float
    risk_level: str
    analysis_summary: str
    created_at: datetime
    recommendations: List[RecommendationResponse] = []
    reasons: List[str] = []

    model_config = ConfigDict(from_attributes=True)

class PolicyAnalysisResponse(BaseModel):
    website_url: Optional[str] = None
    risk_score: float
    risk_level: str
    summary: str
    data_collection: List[str] = []
    third_party_sharing: List[str] = []
    security_concerns: List[str] = []
    recommendations: List[str] = []
