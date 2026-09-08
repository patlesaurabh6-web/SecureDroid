from sqlalchemy import Column, Integer, String, Boolean, Text
from sqlalchemy.orm import relationship
from app.core.database import Base

class Permission(Base):
    __tablename__ = "permissions"

    id = Column(Integer, primary_key=True, index=True)
    permission_name = Column(String(150), unique=True, index=True, nullable=False)
    permission_category = Column(String(100), nullable=False)
    description = Column(Text, nullable=True)
    risk_level = Column(String(20), nullable=False, default="LOW") # LOW, MEDIUM, HIGH, CRITICAL
    sensitive = Column(Boolean, default=False)

    application_permissions = relationship("ApplicationPermission", back_populates="permission")
