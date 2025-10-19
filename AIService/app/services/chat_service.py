from typing import Dict, Any, List, Optional

import httpx
import asyncio
from datetime import datetime
from app.models.ai_models import GetSummaryResponse
class ChatService:
    def __init__(self, rag_service=None):
        self.chat_history = {}  # In-memory storage, replace with database in production
        self.use_rag = True  # Cờ để bật/tắt RAG
        self.rag_service = rag_service
        self.chat_routing = "http://localhost:8080/api/v1/chats"

    async def fetch_summary(self, group_id: str) -> GetSummaryResponse:
        """
        Gọi API Spring Boot để lấy summary
        """
        url = f"{self.chat_routing}/get-summary/{group_id}"
        async with httpx.AsyncClient() as client:
            resp = await client.get(url)
            if resp.status_code == 200:
                data = resp.json()
                print(f"✅ Lấy summary thành công cho group_id {group_id}: {data}")
                return GetSummaryResponse(**data)
            else:
                print(f"⚠️ Lỗi khi gọi summary API: {resp.status_code}")
                return GetSummaryResponse()

    async def get_ai_response(self, message: str, user_id: Optional[str] = None, group_id: Optional[str] = None) -> Dict[str, Any]:
        try:
            context = ""
            if group_id:
                summary_resp = await self.fetch_summary(group_id)
                if summary_resp.summary:
                    context += f"Tóm tắt cuộc hội thoại trước: {summary_resp.summary}\n"
                if summary_resp.messages:
                    context += "Các tin nhắn gần đây:\n" + "\n".join(summary_resp.messages)  # lấy 5 tin gần nhất
            
            # Gửi context + message cho AI (Gemini hoặc simple AI)
            final_prompt = f"{context}\nNgười dùng: {message}"
            
            if self.use_rag and self.rag_service:
                rag_response = await self.rag_service.get_rag_response(final_prompt, user_id)
                if rag_response.get("is_rag_response", False):
                    return rag_response
            
            # fallback
            return await self._get_simple_ai_response(final_prompt, user_id)
        
        except Exception as e:
            print(f"⚠️ Lỗi trong get_ai_response: {e}")
            return await self._get_simple_ai_response(message, user_id)

    
    async def _get_simple_ai_response(self, message: str, user_id: Optional[str] = None, session_id: Optional[str] = None) -> Dict[str, Any]:
        """
        Simple AI response (fallback method)
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
                    "confidence": ai_response.get("confidence"),
                    "is_rag_response": False
                })
            
            return {
                **ai_response,
                "is_rag_response": False
            }
            
        except Exception as e:
            raise Exception(f"Error getting simple AI response: {str(e)}")
    
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
