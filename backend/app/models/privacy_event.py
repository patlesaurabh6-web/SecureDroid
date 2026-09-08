from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Text, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class PrivacyEvent(Base):
    __tablename__ = "privacy_events"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    application_id = Column(Integer, ForeignKey("applications.id", ondelete="SET NULL"), nullable=True)
    event_type = Column(String(100), nullable=False)
    description = Column(Text, nullable=False)
    severity = Column(String(20), nullable=False, default="INFO") # INFO, LOW, MEDIUM, HIGH, CRITICAL
    detected_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="events")
    application = relationship("Application", back_populates="events")
