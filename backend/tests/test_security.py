import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool
import sys
import os

sys.path.insert(0, os.path.realpath(os.path.join(os.path.dirname(__file__), "..")))

import app.models
from app.main import app
from app.core.database import Base, get_db

SQLALCHEMY_DATABASE_URL = "sqlite:///file:memdb_sec?mode=memory&cache=shared&uri=true"
engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool
)
Base.metadata.create_all(bind=engine)

TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def override_get_db():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()

app.dependency_overrides[get_db] = override_get_db
client = TestClient(app)

def test_unauthorized_endpoints():
    res = client.get("/api/dashboard")
    assert res.status_code == 401

    res = client.get("/api/history")
    assert res.status_code == 401

    res = client.get("/api/users/me")
    assert res.status_code == 401

def test_invalid_token():
    headers = {"Authorization": "Bearer invalid_token_12345"}
    res = client.get("/api/dashboard", headers=headers)
    assert res.status_code == 401
