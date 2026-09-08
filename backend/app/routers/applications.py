from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.user import User
from app.models.application import Application, ApplicationPermission
from app.models.permission import Permission
from app.schemas.application import ApplicationCreate, ApplicationResponse
from app.services.auth_service import get_current_user
from app.services.permission_service import classify_permission

router = APIRouter(prefix="/applications", tags=["Applications"])

@router.post("", response_model=ApplicationResponse, status_code=status.HTTP_201_CREATED)
def create_application(
    app_in: ApplicationCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Register or update an application and its requested permissions."""
    db_app = db.query(Application).filter(Application.package_name == app_in.package_name).first()
    if not db_app:
        db_app = Application(
            package_name=app_in.package_name,
            application_name=app_in.application_name,
            version_name=app_in.version_name,
            version_code=app_in.version_code,
            developer=app_in.developer,
            analysis_status="ANALYZED"
        )
        db.add(db_app)
        db.commit()
        db.refresh(db_app)
    else:
        db_app.application_name = app_in.application_name
        db_app.version_name = app_in.version_name
        db_app.version_code = app_in.version_code
        db_app.developer = app_in.developer
        db.commit()

    # Process and link permissions
    for perm_item in app_in.permissions:
        p_name = perm_item.permission_name
        db_perm = db.query(Permission).filter(Permission.permission_name == p_name).first()
        if not db_perm:
            meta = classify_permission(p_name)
            db_perm = Permission(
                permission_name=p_name,
                permission_category=meta["category"],
                description=meta["description"],
                risk_level=meta["risk_level"],
                sensitive=meta["sensitive"]
            )
            db.add(db_perm)
            db.commit()
            db.refresh(db_perm)

        # Link application_permission if not exists
        existing_link = db.query(ApplicationPermission).filter(
            ApplicationPermission.application_id == db_app.id,
            ApplicationPermission.permission_id == db_perm.id
        ).first()

        if not existing_link:
            app_perm = ApplicationPermission(
                application_id=db_app.id,
                permission_id=db_perm.id,
                granted=perm_item.granted
            )
            db.add(app_perm)

    db.commit()
    db.refresh(db_app)
    return db_app

@router.get("", response_model=List[ApplicationResponse])
def get_applications(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Retrieve all scanned applications."""
    apps = db.query(Application).offset(skip).limit(limit).all()
    return apps

@router.get("/{app_id}", response_model=ApplicationResponse)
def get_application_by_id(
    app_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get application details by ID."""
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Application not found")
    return app

@router.delete("/{app_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_application(
    app_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Delete application from catalog."""
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Application not found")
    db.delete(app)
    db.commit()
    return None
