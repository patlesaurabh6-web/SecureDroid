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

@router.get("/status")
def get_monitoring_status():
    return {"status": "active", "service": "realtime_privacy_monitoring"}

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

    desc = event_in.description or event_in.message or event_in.title or "Privacy event recorded"
    sev = event_in.severity or event_in.risk_level or "INFO"
    etype = event_in.event_type or "Permission Change"

    event = PrivacyEvent(
        user_id=current_user.id,
        application_id=app_id,
        event_type=etype,
        description=desc,
        severity=sev
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
