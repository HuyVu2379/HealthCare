from typing import Dict, Any, List, Optional
from app.models.ai_models import ChatMessage, ChatResponse
import asyncio
import json
from datetime import datetime

class ChatService:
    def __init__(self):
        self.chat_history = {}  # In-memory storage, replace with database in production
        
    async def get_ai_response(self, message: str, user_id: Optional[str] = None, session_id: Optional[str] = None) -> Dict[str, Any]:
        """
        Get AI response for user message
        """
        try:
            # Store message in history
            if session_id:
                if session_id not in self.chat_history:
                    self.chat_history[session_id] = []
                
                self.chat_history[session_id].append({
                    "type": "user",
                    "message": message,
                    "timestamp": datetime.now().isoformat(),
                    "user_id": user_id
                })
            
            # Simple AI response logic (replace with actual AI integration)
            ai_response = await self._generate_ai_response(message)
            
            # Store AI response in history
            if session_id:
                self.chat_history[session_id].append({
                    "type": "assistant",
                    "message": ai_response["response"],
                    "timestamp": datetime.now().isoformat(),
                    "confidence": ai_response.get("confidence")
                })
            
            return ai_response
            
        except Exception as e:
            raise Exception(f"Error getting AI response: {str(e)}")
    
    async def _generate_ai_response(self, message: str) -> Dict[str, Any]:
        """
        Generate AI response (placeholder implementation)
        Replace this with actual AI integration (OpenAI, etc.)
        """
        # Simulate processing time
        await asyncio.sleep(0.1)
        
        # Simple keyword-based responses for healthcare
        message_lower = message.lower()
        
        if any(word in message_lower for word in ["headache", "đau đầu"]):
            response = "Đau đầu có thể có nhiều nguyên nhân khác nhau. Một số biện pháp giảm đau đầu bao gồm: nghỉ ngơi trong phòng tối, uống đủ nước, massage nhẹ vùng thái dương. Nếu đau đầu kéo dài hoặc tăng nặng, bạn nên tham khảo ý kiến bác sĩ."
        elif any(word in message_lower for word in ["fever", "sốt"]):
            response = "Sốt là dấu hiệu cơ thể đang chống lại nhiễm trùng. Bạn nên: uống nhiều nước, nghỉ ngơi, có thể dùng thuốc hạ sốt theo hướng dẫn. Nếu sốt trên 39°C hoặc kéo dài quá 3 ngày, hãy đến gặp bác sĩ ngay."
        elif any(word in message_lower for word in ["cough", "ho"]):
            response = "Ho có thể do nhiều nguyên nhân: cảm lạnh, dị ứng, hoặc nhiễm trùng. Bạn có thể: uống nước ấm, ngậm kẹo ho, tránh khói thuốc. Nếu ho có đờm máu hoặc kéo dài quá 2 tuần, hãy khám bác sĩ."
        else:
            response = "Cảm ơn bạn đã chia sẻ. Tôi khuyên bạn nên tham khảo ý kiến của bác sĩ chuyên khoa để được tư vấn chính xác nhất. Tôi có thể hỗ trợ thêm thông tin nào khác không?"
        
        return {
            "response": response,
            "confidence": 0.85
        }
    
    async def get_chat_history(self, session_id: str) -> List[Dict[str, Any]]:
        """
        Get chat history for a session
        """
        return self.chat_history.get(session_id, [])
