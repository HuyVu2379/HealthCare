from fastapi import APIRouter, HTTPException
from app.models.ai_models import ChatMessage, ChatResponse
from app.services.chat_service import ChatService
from datetime import datetime
import uuid

router = APIRouter()
chat_service = ChatService()

@router.post("/", response_model=ChatResponse)
async def chat_with_ai(message: ChatMessage):
    """
    Chat with AI assistant for health-related queries
    """
    try:
        # Generate session ID if not provided
        session_id = message.session_id or str(uuid.uuid4())
        
        # Get AI response
        ai_response = await chat_service.get_ai_response(
            message.message,
            user_id=message.user_id,
            session_id=session_id
        )
        
        return ChatResponse(
            response=ai_response["response"],
            session_id=session_id,
            timestamp=datetime.now(),
            confidence=ai_response.get("confidence")
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/sessions/{session_id}/history")
async def get_chat_history(session_id: str):
    """
    Get chat history for a specific session
    """
    try:
        history = await chat_service.get_chat_history(session_id)
        return {"session_id": session_id, "history": history}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
