from fastapi import APIRouter, HTTPException
from app.models.ai_models import ChatMessage, ChatResponse
from app.services.chat_service import ChatService
from app.services.rag_service import rag_service
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
            confidence=ai_response.get("confidence"),
            sources=ai_response.get("sources"),
            num_sources=ai_response.get("num_sources"),
            is_rag_response=ai_response.get("is_rag_response", False)
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


@router.get("/rag/status")
async def get_rag_status():
    """
    Get RAG system status
    """
    try:
        status = rag_service.get_system_status()
        return {"status": "success", "data": status}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/rag/rebuild")
async def rebuild_rag_vector_store():
    """
    Rebuild RAG vector store
    """
    try:
        result = await rag_service.rebuild_vector_store()
        if result["success"]:
            return {"status": "success", "message": result["message"]}
        else:
            raise HTTPException(status_code=500, detail=result["message"])
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/sessions/{session_id}/history")
async def clear_chat_history(session_id: str):
    """
    Clear chat history for a specific session
    """
    try:
        success = await rag_service.clear_chat_history(session_id)
        if success:
            return {"status": "success", "message": f"Chat history cleared for session {session_id}"}
        else:
            return {"status": "info", "message": f"No history found for session {session_id}"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
