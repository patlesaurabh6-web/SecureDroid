from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Text, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class Recommendation(Base):
    __tablename__ = "recommendations"

    id = Column(Integer, primary_key=True, index=True)
    analysis_id = Column(Integer, ForeignKey("privacy_analyses.id", ondelete="CASCADE"), nullable=False)
    title = Column(String(200), nullable=False)
    description = Column(Text, nullable=False)
    priority = Column(String(20), nullable=False, default="MEDIUM") # LOW, MEDIUM, HIGH, CRITICAL
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    analysis = relationship("PrivacyAnalysis", back_populates="recommendations")
