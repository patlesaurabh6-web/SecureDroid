from pydantic import BaseModel
from typing import Optional

class UserRegister(BaseModel):
    name: str
    email: str
    mobile: Optional[str] = None
    password: str

class UserLogin(BaseModel):
    email: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int
    email: str
    name: str

class TokenPayload(BaseModel):
    sub: Optional[str] = None
