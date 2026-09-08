from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Text, Float, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class PrivacyAnalysis(Base):
    __tablename__ = "privacy_analyses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    application_id = Column(Integer, ForeignKey("applications.id", ondelete="CASCADE"), nullable=True)
    risk_score = Column(Float, nullable=False)
    risk_level = Column(String(20), nullable=False) # LOW, MEDIUM, HIGH, CRITICAL
    analysis_summary = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="analyses")
    application = relationship("Application", back_populates="analyses")
    recommendations = relationship("Recommendation", back_populates="analysis", cascade="all, delete-orphan")
    history_entries = relationship("PrivacyHistory", back_populates="analysis", cascade="all, delete-orphan")
