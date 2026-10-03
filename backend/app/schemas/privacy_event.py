from pydantic import BaseModel, ConfigDict
from typing import Optional
from datetime import datetime

class PrivacyEventCreate(BaseModel):
    package_name: Optional[str] = None
    event_type: Optional[str] = "Permission Change"
    description: Optional[str] = None
    message: Optional[str] = None
    severity: Optional[str] = "INFO"
    risk_level: Optional[str] = None
    title: Optional[str] = None

class PrivacyEventResponse(BaseModel):
    id: int
    user_id: int
    application_id: Optional[int]
    event_type: str
    description: str
    severity: str
    detected_at: datetime

    model_config = ConfigDict(from_attributes=True)
