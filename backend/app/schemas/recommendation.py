from pydantic import BaseModel, ConfigDict
from datetime import datetime

class RecommendationResponse(BaseModel):
    id: int
    analysis_id: int
    title: str
    description: str
    priority: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
