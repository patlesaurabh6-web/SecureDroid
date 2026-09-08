from pydantic import BaseModel, ConfigDict
from typing import Optional
from datetime import datetime

class UserBase(BaseModel):
    name: str
    email: str
    mobile: Optional[str] = None

class UserUpdate(BaseModel):
    name: Optional[str] = None
    mobile: Optional[str] = None
    password: Optional[str] = None

class UserResponse(UserBase):
    id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
