from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Boolean, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class Application(Base):
    __tablename__ = "applications"

    id = Column(Integer, primary_key=True, index=True)
    package_name = Column(String(255), unique=True, index=True, nullable=False)
    application_name = Column(String(255), nullable=False)
    version_name = Column(String(50), nullable=True)
    version_code = Column(Integer, nullable=True)
    developer = Column(String(255), nullable=True)
    analysis_status = Column(String(50), default="PENDING") # PENDING, ANALYZED, ERROR
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now(), server_default=func.now())

    app_permissions = relationship("ApplicationPermission", back_populates="application", cascade="all, delete-orphan")
    analyses = relationship("PrivacyAnalysis", back_populates="application", cascade="all, delete-orphan")
    events = relationship("PrivacyEvent", back_populates="application", cascade="all, delete-orphan")

class ApplicationPermission(Base):
    __tablename__ = "application_permissions"

    id = Column(Integer, primary_key=True, index=True)
    application_id = Column(Integer, ForeignKey("applications.id", ondelete="CASCADE"), nullable=False)
    permission_id = Column(Integer, ForeignKey("permissions.id", ondelete="CASCADE"), nullable=False)
    granted = Column(Boolean, default=True)
    detected_at = Column(DateTime(timezone=True), server_default=func.now())

    application = relationship("Application", back_populates="app_permissions")
    permission = relationship("Permission", back_populates="application_permissions")
