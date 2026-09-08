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

SQLALCHEMY_DATABASE_URL = "sqlite:///file:memdb_apps?mode=memory&cache=shared&uri=true"
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

def get_auth_token():
    client.post("/api/auth/register", json={
        "name": "App Tester",
        "email": "apptester@securedroid.test",
        "password": "Password123!"
    })
    res = client.post("/api/auth/login", json={
        "email": "apptester@securedroid.test",
        "password": "Password123!"
    })
    return res.json()["access_token"]

def test_application_submission_and_analysis():
    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    # 1. Analyze Application
    analysis_res = client.post(
        "/api/analysis/application",
        headers=headers,
        json={
            "package_name": "com.example.riskyapp",
            "application_name": "Risky Test App",
            "version_name": "2.1.0",
            "developer": "Test Dev",
            "permissions": [
                "android.permission.CAMERA",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.READ_CONTACTS"
            ]
        }
    )
    assert analysis_res.status_code == 201
    data = analysis_res.json()
    assert data["risk_level"] in ["HIGH", "CRITICAL"]
    assert len(data["recommendations"]) > 0

    # 2. Check Dashboard Summary
    dash_res = client.get("/api/dashboard", headers=headers)
    assert dash_res.status_code == 200
    dash_data = dash_res.json()
    assert dash_data["total_applications"] >= 1
    assert dash_data["high_risk_applications"] >= 1
