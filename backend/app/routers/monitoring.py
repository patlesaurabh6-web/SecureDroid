from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.user import User
from app.models.application import Application
from app.models.privacy_event import PrivacyEvent
from app.models.history import PrivacyHistory
from app.schemas.privacy_event import PrivacyEventCreate, PrivacyEventResponse
from app.services.auth_service import get_current_user

router = APIRouter(prefix="/monitoring", tags=["Monitoring & Events"])

@router.post("/events", response_model=PrivacyEventResponse, status_code=status.HTTP_201_CREATED)
def record_privacy_event(
    event_in: PrivacyEventCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Receives privacy events reported by the Android foreground/background monitor service.
    Stores the event and adds an entry to user's privacy history.
    """
    app_id = None
    if event_in.package_name:
        app = db.query(Application).filter(Application.package_name == event_in.package_name).first()
        if app:
            app_id = app.id

    event = PrivacyEvent(
        user_id=current_user.id,
        application_id=app_id,
        event_type=event_in.event_type,
        description=event_in.description,
        severity=event_in.severity
    )
    db.add(event)
    db.commit()
    db.refresh(event)

    # Log into history
    history = PrivacyHistory(
        user_id=current_user.id,
        application_id=app_id,
        event_type=f"Event: {event_in.event_type}"
    )
    db.add(history)
    db.commit()

    return event

@router.get("/events", response_model=List[PrivacyEventResponse])
def get_privacy_events(
    skip: int = 0,
    limit: int = 50,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Retrieve list of privacy events for current user."""
    events = db.query(PrivacyEvent).filter(
        PrivacyEvent.user_id == current_user.id
    ).order_by(PrivacyEvent.detected_at.desc()).offset(skip).limit(limit).all()
    return events
