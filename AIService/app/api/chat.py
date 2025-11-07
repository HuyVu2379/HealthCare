from fastapi import APIRouter, HTTPException, Request
from app.models.ai_models import ChatMessage, ChatResponse, SimpleChatResponse
from app.services.chat_service import ChatService
from datetime import datetime
router = APIRouter()
@router.post("/ask", response_model=SimpleChatResponse)
async def chat_with_ai(message: ChatMessage, request: Request):
    """
    Chat with AI assistant for health-related queries
    """
    try:
        # Get RAG service from app state
        rag_service = request.app.state.rag_service
        chat_service = ChatService(rag_service=rag_service)
        
        # Get AI response
        ai_response = await chat_service.get_ai_response(
            message.message,
            user_id=message.user_id,
            group_id=message.group_id,
        )
        
        return SimpleChatResponse(
            response=ai_response["response"],
            confidence=ai_response.get("confidence")
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
