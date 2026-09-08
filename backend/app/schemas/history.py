from pydantic import BaseModel, ConfigDict
from typing import Optional
from datetime import datetime

class HistoryResponse(BaseModel):
    id: int
    user_id: int
    application_id: Optional[int]
    application_name: Optional[str] = "System / Event"
    analysis_id: Optional[int]
    event_type: str
    risk_score: Optional[float]
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
