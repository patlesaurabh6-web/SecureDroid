from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.models.user import User
from app.models.application import Application
from app.models.analysis import PrivacyAnalysis
from app.models.privacy_event import PrivacyEvent
from app.schemas.dashboard import DashboardSummary
from app.schemas.privacy_event import PrivacyEventResponse
from app.schemas.analysis import AnalysisResponse
from app.schemas.recommendation import RecommendationResponse
from app.services.auth_service import get_current_user
from app.services.risk_service import calculate_privacy_risk

router = APIRouter(prefix="/dashboard", tags=["Dashboard"])

@router.get("", response_model=DashboardSummary)
def get_dashboard_summary(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Returns unified privacy statistics, overall device risk score,
    recent alerts, recent analyses, and top AI/privacy recommendations.
    """
    # Fetch recent analyses for user
    user_analyses = db.query(PrivacyAnalysis).filter(
        PrivacyAnalysis.user_id == current_user.id
    ).order_by(PrivacyAnalysis.created_at.desc()).limit(10).all()

    total_apps = db.query(Application).count()
    
    high_count = 0
    med_count = 0
    low_count = 0
    total_score_sum = 0.0

    if user_analyses:
        for a in user_analyses:
            total_score_sum += a.risk_score
            if a.risk_level in ["HIGH", "CRITICAL"]:
                high_count += 1
            elif a.risk_level == "MEDIUM":
                med_count += 1
            else:
                low_count += 1
        
        overall_score = round(total_score_sum / len(user_analyses), 1)
    else:
        overall_score = 15.0 # Baseline safe score if no apps analyzed yet
        low_count = total_apps

    if overall_score <= 30:
        overall_risk_level = "LOW"
    elif overall_score <= 60:
        overall_risk_level = "MEDIUM"
    elif overall_score <= 80:
        overall_risk_level = "HIGH"
    else:
        overall_risk_level = "CRITICAL"

    # Fetch recent events
    recent_events = db.query(PrivacyEvent).filter(
        PrivacyEvent.user_id == current_user.id
    ).order_by(PrivacyEvent.detected_at.desc()).limit(5).all()

    event_responses = [PrivacyEventResponse.model_validate(e) for e in recent_events]
    analysis_responses = []

    for a in user_analyses[:5]:
        reasons = []
        if a.application:
            perms = [p.permission.permission_name for p in a.application.app_permissions if p.permission]
            _, _, reasons = calculate_privacy_risk(perms)
            
        analysis_responses.append(AnalysisResponse(
            id=a.id,
            user_id=a.user_id,
            application_id=a.application_id,
            risk_score=a.risk_score,
            risk_level=a.risk_level,
            analysis_summary=a.analysis_summary,
            created_at=a.created_at,
            recommendations=[RecommendationResponse.model_validate(r) for r in a.recommendations],
            reasons=reasons
        ))

    rec_title = "Privacy Recommendation"
    rec_summary = "Regularly inspect applications requesting Camera, Location, and Contacts permissions."

    if high_count > 0:
        rec_title = "High Risk Applications Detected"
        rec_summary = f"You have {high_count} application(s) with sensitive permission access. Review permissions in settings."

    return DashboardSummary(
        overall_risk_score=overall_score,
        risk_level=overall_risk_level,
        total_applications=total_apps,
        high_risk_applications=high_count,
        medium_risk_applications=med_count,
        low_risk_applications=low_count,
        recent_events=event_responses,
        recent_analyses=analysis_responses,
        recommendation_title=rec_title,
        recommendation_summary=rec_summary
    )
