from pydantic import BaseModel, ConfigDict
from typing import Optional

class PermissionBase(BaseModel):
    permission_name: str
    permission_category: str
    description: Optional[str] = None
    risk_level: str = "LOW"
    sensitive: bool = False

class PermissionCreate(PermissionBase):
    pass

class PermissionResponse(PermissionBase):
    id: int

    model_config = ConfigDict(from_attributes=True)

class ApplicationPermissionCreate(BaseModel):
    permission_name: str
    granted: bool = True
