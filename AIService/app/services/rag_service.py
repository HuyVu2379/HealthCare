"""
RAG Service để tích hợp RAG PDF Chatbot vào API
"""

import os
import sys
from pathlib import Path
from typing import Dict, Any, List, Optional
import asyncio
from datetime import datetime

# Thêm đường dẫn để import RAG chatbot
sys.path.append(str(Path(__file__).parent.parent.parent / "rag_chatbot"))

try:
    from rag_pdf_chatbot import RAGPDFChatbot
except ImportError as e:
    print(f"Warning: Could not import RAGPDFChatbot: {e}")
    RAGPDFChatbot = None


class RAGService:
    """
    Service để xử lý RAG chatbot requests
    """
    
    def __init__(self):
        self.chatbot = None
        self.is_initialized = False
        self.initialization_error = None
        self.chat_history = {}  # In-memory storage
        
    async def initialize(self):
        """
        Khởi tạo RAG chatbot
        """
        if self.is_initialized:
            return True
            
        try:
            if RAGPDFChatbot is None:
                self.initialization_error = "RAGPDFChatbot class not available"
                return False
                
            # Thiết lập đường dẫn
            current_dir = Path(__file__).parent.parent.parent
            pdf_dir = current_dir / "rag_chatbot" / "data"
            vector_store_dir = current_dir / "rag_chatbot" / "vector_store"
            
            # Kiểm tra thư mục PDF
            if not pdf_dir.exists():
                self.initialization_error = f"PDF directory not found: {pdf_dir}"
                return False
                
            # Kiểm tra có file PDF không
            pdf_files = list(pdf_dir.glob("*.pdf"))
            if not pdf_files:
                self.initialization_error = f"No PDF files found in: {pdf_dir}"
                return False
            
            # Khởi tạo chatbot
            self.chatbot = RAGPDFChatbot(
                pdf_directory=str(pdf_dir),
                vector_store_path=str(vector_store_dir),
                chunk_size=1000,
                chunk_overlap=200
            )
            
            # Khởi tạo hệ thống
            success = await asyncio.to_thread(self.chatbot.initialize_system)
            if not success:
                self.initialization_error = "Failed to initialize RAG system"
                return False
                
            self.is_initialized = True
            return True
            
        except Exception as e:
            self.initialization_error = f"Error initializing RAG service: {str(e)}"
            return False
    
    async def get_rag_response(self, message: str, user_id: Optional[str] = None, session_id: Optional[str] = None) -> Dict[str, Any]:
        """
        Lấy phản hồi từ RAG chatbot
        """
        try:
            # Kiểm tra khởi tạo
            if not self.is_initialized:
                await self.initialize()
                
            if not self.is_initialized:
                return {
                    "response": f"Lỗi: RAG chatbot chưa được khởi tạo. {self.initialization_error}",
                    "confidence": 0.0,
                    "is_rag_response": False
                }
            
            # Lưu tin nhắn người dùng vào lịch sử
            if session_id:
                if session_id not in self.chat_history:
                    self.chat_history[session_id] = []
                
                self.chat_history[session_id].append({
                    "type": "user",
                    "message": message,
                    "timestamp": datetime.now().isoformat(),
                    "user_id": user_id
                })
            
            # Lấy phản hồi từ RAG chatbot
            result = await asyncio.to_thread(self.chatbot.ask_question, message)
            
            # Lưu phản hồi AI vào lịch sử
            if session_id:
                self.chat_history[session_id].append({
                    "type": "assistant",
                    "message": result['response'],
                    "timestamp": datetime.now().isoformat(),
                    "confidence": result.get('confidence', 0.0),
                    "is_rag_response": True
                })
            
            return {
                "response": result['response'],
                "confidence": result.get('confidence', 0.0),
                "is_rag_response": True
            }
            
        except Exception as e:
            error_response = f"Lỗi khi xử lý câu hỏi với RAG: {str(e)}"
            
            # Lưu lỗi vào lịch sử
            if session_id and session_id in self.chat_history:
                self.chat_history[session_id].append({
                    "type": "error",
                    "message": error_response,
                    "timestamp": datetime.now().isoformat(),
                    "error": str(e)
                })
            
            return {
                "response": error_response,
                "confidence": 0.0,
                "is_rag_response": False
            }
    
    async def get_chat_history(self, session_id: str) -> List[Dict[str, Any]]:
        """
        Lấy lịch sử chat cho một session
        """
        return self.chat_history.get(session_id, [])
    
    async def clear_chat_history(self, session_id: str) -> bool:
        """
        Xóa lịch sử chat cho một session
        """
        if session_id in self.chat_history:
            del self.chat_history[session_id]
            return True
        return False
    
    async def rebuild_vector_store(self) -> Dict[str, Any]:
        """
        Tái tạo vector store
        """
        try:
            if not self.is_initialized or not self.chatbot:
                return {
                    "success": False,
                    "message": "RAG chatbot chưa được khởi tạo"
                }
            
            # Tái tạo vector store
            success = await asyncio.to_thread(self.chatbot.initialize_system, True)
            
            return {
                "success": success,
                "message": "Tái tạo vector store thành công" if success else "Lỗi khi tái tạo vector store"
            }
            
        except Exception as e:
            return {
                "success": False,
                "message": f"Lỗi: {str(e)}"
            }
    
    def get_system_status(self) -> Dict[str, Any]:
        """
        Lấy trạng thái hệ thống RAG
        """
        return {
            "is_initialized": self.is_initialized,
            "initialization_error": self.initialization_error,
            "has_chatbot": self.chatbot is not None,
            "active_sessions": len(self.chat_history),
            "total_messages": sum(len(history) for history in self.chat_history.values())
        }


# Global instance
rag_service = RAGService()
