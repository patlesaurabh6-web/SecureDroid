from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_password_hash
from app.models.user import User
from app.schemas.user import UserResponse, UserUpdate
from app.services.auth_service import get_current_user

router = APIRouter(prefix="/users", tags=["Users"])

@router.get("/me", response_model=UserResponse)
def read_current_user(current_user: User = Depends(get_current_user)):
    """Retrieve details for the current user."""
    return current_user

@router.put("/me", response_model=UserResponse)
def update_current_user(
    user_in: UserUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Update name, mobile, or password for the authenticated user."""
    if user_in.name is not None:
        current_user.name = user_in.name
    if user_in.mobile is not None:
        current_user.mobile = user_in.mobile
    if user_in.password is not None and len(user_in.password) >= 6:
        current_user.password_hash = get_password_hash(user_in.password)

    db.add(current_user)
    db.commit()
    db.refresh(current_user)
    return current_user

@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_current_user(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Delete current user account and all associated privacy data."""
    db.delete(current_user)
    db.commit()
    return None
