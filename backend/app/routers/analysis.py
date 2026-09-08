from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.user import User
from app.models.application import Application, ApplicationPermission
from app.models.permission import Permission
from app.models.analysis import PrivacyAnalysis
from app.models.recommendation import Recommendation
from app.models.history import PrivacyHistory
from app.schemas.analysis import AppAnalysisRequest, PolicyAnalysisRequest, AnalysisResponse, PolicyAnalysisResponse
from app.schemas.recommendation import RecommendationResponse
from app.services.auth_service import get_current_user
from app.services.risk_service import calculate_privacy_risk
from app.services.recommendation_service import generate_recommendations
from app.services.permission_service import classify_permission
from app.services.ai_service import analyze_privacy_policy_with_ai

router = APIRouter(prefix="/analysis", tags=["Analysis"])

@router.post("/application", response_model=AnalysisResponse, status_code=status.HTTP_201_CREATED)
def analyze_application(
    req: AppAnalysisRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Submits application package details and permission list for privacy risk evaluation.
    Calculates score (0-100), stores result in MySQL, generates recommendations and history entry.
    """
    # Find or create application
    app = db.query(Application).filter(Application.package_name == req.package_name).first()
    if not app:
        app = Application(
            package_name=req.package_name,
            application_name=req.application_name,
            version_name=req.version_name,
            developer=req.developer,
            analysis_status="ANALYZED"
        )
        db.add(app)
        db.commit()
        db.refresh(app)
    else:
        app.application_name = req.application_name
        app.version_name = req.version_name
        app.developer = req.developer
        db.commit()

    # Calculate privacy score & risk level
    score, risk_level, reasons = calculate_privacy_risk(req.permissions)

    summary_str = f"Privacy risk evaluated as {risk_level} (Score: {score}/100) based on {len(req.permissions)} permissions."

    analysis = PrivacyAnalysis(
        user_id=current_user.id,
        application_id=app.id,
        risk_score=score,
        risk_level=risk_level,
        analysis_summary=summary_str
    )
    db.add(analysis)
    db.commit()
    db.refresh(analysis)

    # Generate and link recommendations
    rec_list = generate_recommendations(req.permissions, risk_level)
    db_recs = []
    for rec_data in rec_list:
        db_rec = Recommendation(
            analysis_id=analysis.id,
            title=rec_data["title"],
            description=rec_data["description"],
            priority=rec_data["priority"]
        )
        db.add(db_rec)
        db_recs.append(db_rec)

    # Create history entry
    history = PrivacyHistory(
        user_id=current_user.id,
        application_id=app.id,
        analysis_id=analysis.id,
        event_type=f"Application Analysis ({app.application_name})",
        risk_score=score
    )
    db.add(history)
    db.commit()
    db.refresh(analysis)

    response = AnalysisResponse(
        id=analysis.id,
        user_id=analysis.user_id,
        application_id=analysis.application_id,
        risk_score=analysis.risk_score,
        risk_level=analysis.risk_level,
        analysis_summary=analysis.analysis_summary,
        created_at=analysis.created_at,
        recommendations=[RecommendationResponse.model_validate(r) for r in analysis.recommendations],
        reasons=reasons
    )
    return response

@router.post("/privacy-policy", response_model=PolicyAnalysisResponse)
def analyze_privacy_policy(
    req: PolicyAnalysisRequest,
    current_user: User = Depends(get_current_user)
):
    """
    Parses and evaluates Privacy Policy text for data collection, sharing, and security concerns using AI.
    """
    if not req.privacy_policy_text or len(req.privacy_policy_text.strip()) < 10:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Privacy policy text too short for analysis.")
    
    res = analyze_privacy_policy_with_ai(req.privacy_policy_text, req.website_url)
    return PolicyAnalysisResponse(**res)

@router.get("/{analysis_id}", response_model=AnalysisResponse)
def get_analysis(
    analysis_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Retrieve privacy analysis by ID."""
    analysis = db.query(PrivacyAnalysis).filter(
        PrivacyAnalysis.id == analysis_id,
        PrivacyAnalysis.user_id == current_user.id
    ).first()
    if not analysis:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Analysis not found")
    
    reasons = []
    if analysis.application:
        perms = [p.permission.permission_name for p in analysis.application.app_permissions if p.permission]
        _, _, reasons = calculate_privacy_risk(perms)

    return AnalysisResponse(
        id=analysis.id,
        user_id=analysis.user_id,
        application_id=analysis.application_id,
        risk_score=analysis.risk_score,
        risk_level=analysis.risk_level,
        analysis_summary=analysis.analysis_summary,
        created_at=analysis.created_at,
        recommendations=[RecommendationResponse.model_validate(r) for r in analysis.recommendations],
        reasons=reasons
    )

@router.get("/{analysis_id}/recommendations", response_model=List[RecommendationResponse])
def get_analysis_recommendations(
    analysis_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Retrieve recommendations for a specific analysis."""
    analysis = db.query(PrivacyAnalysis).filter(
        PrivacyAnalysis.id == analysis_id,
        PrivacyAnalysis.user_id == current_user.id
    ).first()
    if not analysis:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Analysis not found")
    return [RecommendationResponse.model_validate(r) for r in analysis.recommendations]
