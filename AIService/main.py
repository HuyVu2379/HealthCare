from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.api import health, chat, analysis
from app.core.config import settings
from app.services.eureka_service import eureka_service
from app.services.rag_service import RAGService
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global RAG service instance
rag_service = RAGService()

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    try:
        logger.info("Starting AI Service...")
        
        # Start Eureka service
        await eureka_service.start_eureka_client()
        logger.info("Eureka service started successfully")
        
        # Initialize RAG service
        logger.info("Initializing RAG PDF Chatbot...")
        if await rag_service.initialize():
            logger.info("RAG PDF Chatbot initialized successfully")
        else:
            logger.warning(f"RAG PDF Chatbot initialization failed: {rag_service.initialization_error}")
        
        logger.info("AI Service started successfully")
    except Exception as e:
        logger.error(f"Failed to start AI Service: {str(e)}")
    
    yield
    
    # Shutdown
    try:
        logger.info("Shutting down AI Service...")
        await eureka_service.stop_eureka_client()
        logger.info("AI Service shut down successfully")
    except Exception as e:
        logger.error(f"Error during AI Service shutdown: {str(e)}")

app = FastAPI(
    title="AI Service",
    description="AI Service for Healthcare System",
    version="1.0.0",
    lifespan=lifespan
)

# Make RAG service available to the app
app.state.rag_service = rag_service

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, replace with specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router, prefix="/api/v1/health", tags=["health"])
app.include_router(chat.router, prefix="/api/v1/chat", tags=["chat"])
app.include_router(analysis.router, prefix="/api/v1/analysis", tags=["analysis"])

@app.get("/")
async def root():
    return {"message": "AI Service for Healthcare System", "version": "1.0.0"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8086)
