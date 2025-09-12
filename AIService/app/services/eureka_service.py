import asyncio
import logging
from typing import Optional
from py_eureka_client import eureka_client
from app.core.config import settings

logger = logging.getLogger(__name__)

class EurekaService:
    def __init__(self):
        self.eureka_client: Optional[eureka_client.EurekaClient] = None
        self.is_registered = False
    
    async def start_eureka_client(self):
        """Initialize and start Eureka client"""
        try:
            await eureka_client.init_async(
                eureka_server=settings.EUREKA_SERVER_URL,
                app_name=settings.SERVICE_NAME,
                instance_port=settings.SERVICE_PORT,
                instance_host=settings.SERVICE_HOST,
                # Health check URL
                health_check_url=f"http://{settings.SERVICE_HOST}:{settings.SERVICE_PORT}/api/v1/health",
                # Status page URL
                status_page_url=f"http://{settings.SERVICE_HOST}:{settings.SERVICE_PORT}/api/v1/health/status",
                # Home page URL
                home_page_url=f"http://{settings.SERVICE_HOST}:{settings.SERVICE_PORT}",
                # Renewal interval in seconds (default: 30)
                renewal_interval_in_secs=30,
                # Duration in seconds (default: 90)
                duration_in_secs=90,
                # Instance metadata
                metadata={
                    "management.port": str(settings.SERVICE_PORT),
                    "version": "1.0.0",
                    "description": "AI Service for Healthcare System"
                }
            )
            self.is_registered = True
            logger.info(f"Successfully registered {settings.SERVICE_NAME} with Eureka Server at {settings.EUREKA_SERVER_URL}")
        except Exception as e:
            logger.error(f"Failed to register with Eureka Server: {str(e)}")
            self.is_registered = False
            raise e
    
    async def stop_eureka_client(self):
        """Stop and deregister from Eureka"""
        try:
            if self.is_registered:
                await eureka_client.stop_async()
                self.is_registered = False
                logger.info(f"Successfully deregistered {settings.SERVICE_NAME} from Eureka Server")
        except Exception as e:
            logger.error(f"Error during Eureka deregistration: {str(e)}")
    
    def get_service_url(self, service_name: str) -> Optional[str]:
        """Get service URL from Eureka"""
        try:
            return eureka_client.get_service_url(service_name)
        except Exception as e:
            logger.error(f"Failed to get service URL for {service_name}: {str(e)}")
            return None
    
    def get_all_services(self) -> dict:
        """Get all registered services from Eureka"""
        try:
            return eureka_client.get_applications()
        except Exception as e:
            logger.error(f"Failed to get all services: {str(e)}")
            return {}

# Global instance
eureka_service = EurekaService()
