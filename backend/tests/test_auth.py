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

SQLALCHEMY_DATABASE_URL = "sqlite:///file:memdb_auth?mode=memory&cache=shared&uri=true"
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

def test_register_and_login_flow():
    # 1. Register User
    reg_response = client.post(
        "/api/auth/register",
        json={
            "name": "Test Security User",
            "email": "user@securedroid.test",
            "mobile": "1234567890",
            "password": "Password123!"
        }
    )
    assert reg_response.status_code == 201
    reg_data = reg_response.json()
    assert reg_data["email"] == "user@securedroid.test"
    assert "id" in reg_data

    # 2. Duplicate Registration Prevention
    dup_response = client.post(
        "/api/auth/register",
        json={
            "name": "Test Security User",
            "email": "user@securedroid.test",
            "password": "Password123!"
        }
    )
    assert dup_response.status_code == 409

    # 3. Login
    login_response = client.post(
        "/api/auth/login",
        json={
            "email": "user@securedroid.test",
            "password": "Password123!"
        }
    )
    assert login_response.status_code == 200
    token_data = login_response.json()
    assert "access_token" in token_data
    token = token_data["access_token"]

    # 4. Get Current User (/api/auth/me)
    me_response = client.get(
        "/api/auth/me",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert me_response.status_code == 200
    assert me_response.json()["email"] == "user@securedroid.test"

def test_invalid_login():
    response = client.post(
        "/api/auth/login",
        json={
            "email": "nonexistent@securedroid.test",
            "password": "WrongPassword"
        }
    )
    assert response.status_code == 401
