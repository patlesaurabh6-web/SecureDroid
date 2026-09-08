from pydantic import BaseModel, ConfigDict
from typing import Optional
from datetime import datetime

class PrivacyEventCreate(BaseModel):
    package_name: Optional[str] = None
    event_type: str
    description: str
    severity: str = "INFO"

class PrivacyEventResponse(BaseModel):
    id: int
    user_id: int
    application_id: Optional[int]
    event_type: str
    description: str
    severity: str
    detected_at: datetime

    model_config = ConfigDict(from_attributes=True)
