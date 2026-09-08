from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.user import User
from app.models.permission import Permission
from app.schemas.permission import PermissionResponse, PermissionCreate
from app.services.auth_service import get_current_user
from app.services.permission_service import classify_permission

router = APIRouter(prefix="/permissions", tags=["Permissions"])

@router.get("", response_model=List[PermissionResponse])
def list_permissions(
    skip: int = 0,
    limit: int = 200,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """List categorized Android permissions catalog."""
    permissions = db.query(Permission).offset(skip).limit(limit).all()
    return permissions

@router.post("", response_model=PermissionResponse, status_code=status.HTTP_201_CREATED)
def create_permission(
    perm_in: PermissionCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Add a new permission definition to database catalog."""
    existing = db.query(Permission).filter(Permission.permission_name == perm_in.permission_name).first()
    if existing:
        return existing
    
    db_perm = Permission(
        permission_name=perm_in.permission_name,
        permission_category=perm_in.permission_category,
        description=perm_in.description,
        risk_level=perm_in.risk_level,
        sensitive=perm_in.sensitive
    )
    db.add(db_perm)
    db.commit()
    db.refresh(db_perm)
    return db_perm
