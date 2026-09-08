from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Float, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class PrivacyHistory(Base):
    __tablename__ = "privacy_history"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    application_id = Column(Integer, ForeignKey("applications.id", ondelete="SET NULL"), nullable=True)
    analysis_id = Column(Integer, ForeignKey("privacy_analyses.id", ondelete="SET NULL"), nullable=True)
    event_type = Column(String(100), nullable=False)
    risk_score = Column(Float, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="history")
    analysis = relationship("PrivacyAnalysis", back_populates="history_entries")
