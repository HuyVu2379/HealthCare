from fastapi import APIRouter
from app.services.eureka_service import eureka_service

router = APIRouter()

@router.get("/")
async def health_check():
    return {
        "status": "healthy", 
        "service": "AI Service",
        "eureka_registered": eureka_service.is_registered
    }

@router.get("/status")
async def status_check():
    """Status page for Eureka"""
    return {
        "status": "UP",
        "service": "AI Service",
        "eureka_registered": eureka_service.is_registered,
        "version": "1.0.0"
    }

@router.get("/readiness")
async def readiness_check():
    # Add any dependency checks here (database, external APIs, etc.)
    return {
        "status": "ready", 
        "service": "AI Service",
        "eureka_registered": eureka_service.is_registered
    }

@router.get("/liveness")
async def liveness_check():
    return {
        "status": "alive", 
        "service": "AI Service",
        "eureka_registered": eureka_service.is_registered
    }

@router.get("/eureka")
async def eureka_status():
    """Get Eureka registration status and available services"""
    return {
        "registered": eureka_service.is_registered,
        "available_services": eureka_service.get_all_services() if eureka_service.is_registered else {}
    }
