from app.routers.auth import router as auth_router
from app.routers.users import router as users_router
from app.routers.applications import router as applications_router
from app.routers.permissions import router as permissions_router
from app.routers.analysis import router as analysis_router
from app.routers.monitoring import router as monitoring_router
from app.routers.history import router as history_router
from app.routers.dashboard import router as dashboard_router

__all__ = [
    "auth_router",
    "users_router",
    "applications_router",
    "permissions_router",
    "analysis_router",
    "monitoring_router",
    "history_router",
    "dashboard_router"
]
