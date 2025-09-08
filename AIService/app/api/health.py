from fastapi import APIRouter

router = APIRouter()

@router.get("/")
async def health_check():
    return {"status": "healthy", "service": "AI Service"}

@router.get("/readiness")
async def readiness_check():
    # Add any dependency checks here (database, external APIs, etc.)
    return {"status": "ready", "service": "AI Service"}

@router.get("/liveness")
async def liveness_check():
    return {"status": "alive", "service": "AI Service"}
