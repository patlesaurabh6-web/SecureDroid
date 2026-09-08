from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.user import User
from app.models.history import PrivacyHistory
from app.schemas.history import HistoryResponse
from app.services.auth_service import get_current_user

router = APIRouter(prefix="/history", tags=["Privacy History"])

@router.get("", response_model=List[HistoryResponse])
def get_user_history(
    skip: int = 0,
    limit: int = 50,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Retrieve chronological privacy audit history for current user."""
    history_entries = db.query(PrivacyHistory).filter(
        PrivacyHistory.user_id == current_user.id
    ).order_by(PrivacyHistory.created_at.desc()).offset(skip).limit(limit).all()

    result = []
    for item in history_entries:
        app_name = "System / Privacy Event"
        if item.analysis and item.analysis.application:
            app_name = item.analysis.application.application_name
        
        result.append(HistoryResponse(
            id=item.id,
            user_id=item.user_id,
            application_id=item.application_id,
            application_name=app_name,
            analysis_id=item.analysis_id,
            event_type=item.event_type,
            risk_score=item.risk_score,
            created_at=item.created_at
        ))
    return result

@router.get("/{history_id}", response_model=HistoryResponse)
def get_history_entry(
    history_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Get single history entry by ID."""
    entry = db.query(PrivacyHistory).filter(
        PrivacyHistory.id == history_id,
        PrivacyHistory.user_id == current_user.id
    ).first()

    if not entry:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="History entry not found")

    app_name = "System / Privacy Event"
    if entry.analysis and entry.analysis.application:
        app_name = entry.analysis.application.application_name

    return HistoryResponse(
        id=entry.id,
        user_id=entry.user_id,
        application_id=entry.application_id,
        application_name=app_name,
        analysis_id=entry.analysis_id,
        event_type=entry.event_type,
        risk_score=entry.risk_score,
        created_at=entry.created_at
    )
