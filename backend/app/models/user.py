from sqlalchemy import Column, Integer, String, DateTime, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    email = Column(String(150), unique=True, index=True, nullable=False)
    mobile = Column(String(20), nullable=True)
    password_hash = Column(String(255), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now(), server_default=func.now())

    analyses = relationship("PrivacyAnalysis", back_populates="user", cascade="all, delete-orphan")
    events = relationship("PrivacyEvent", back_populates="user", cascade="all, delete-orphan")
    history = relationship("PrivacyHistory", back_populates="user", cascade="all, delete-orphan")
