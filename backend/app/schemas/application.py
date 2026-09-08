from pydantic import BaseModel, ConfigDict
from typing import Optional, List
from datetime import datetime
from app.schemas.permission import PermissionResponse, ApplicationPermissionCreate

class ApplicationCreate(BaseModel):
    package_name: str
    application_name: str
    version_name: Optional[str] = "1.0.0"
    version_code: Optional[int] = 1
    developer: Optional[str] = "Unknown"
    permissions: List[ApplicationPermissionCreate] = []

class ApplicationResponse(BaseModel):
    id: int
    package_name: str
    application_name: str
    version_name: Optional[str]
    version_code: Optional[int]
    developer: Optional[str]
    analysis_status: str
    created_at: datetime
    permissions: List[PermissionResponse] = []

    model_config = ConfigDict(from_attributes=True)
