from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    # API Configuration
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "AI_Service"
    
    # OpenAI Configuration
    OPENAI_API_KEY: Optional[str] = None
    
    # Eureka Configuration
    EUREKA_SERVER_URL: str = "http://localhost:8761/eureka"
    SERVICE_NAME: str = "ai-service"
    SERVICE_PORT: int = 8086
    SERVICE_HOST: str = "localhost"
    
    # # Database Configuration (if needed)
    # DATABASE_URL: Optional[str] = None
    
    # Security
    SECRET_KEY: str = "your-secret-key-here"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    class Config:
        env_file = ".env"

settings = Settings()
