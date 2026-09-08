from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from app.core.config import settings
import logging

logger = logging.getLogger(__name__)

db_url = settings.get_database_url()
Base = declarative_base()

connect_args = {}
if db_url.startswith("sqlite"):
    connect_args = {"check_same_thread": False}

use_fallback = False

try:
    engine = create_engine(
        db_url,
        connect_args=connect_args,
        pool_pre_ping=True
    )
    # Test connection immediately to verify credentials and DB availability
    with engine.connect() as conn:
        pass
    logger.info(f"Successfully connected to primary database at {db_url}")
except Exception as e:
    logger.warning(f"Primary database connection failed for {db_url}: {e}. Switching to local SQLite database fallback.")
    use_fallback = True

if use_fallback:
    fallback_url = "sqlite:///./privacy_monitoring.db"
    engine = create_engine(fallback_url, connect_args={"check_same_thread": False})

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
