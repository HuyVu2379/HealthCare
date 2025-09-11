"""
Test script cho RAG Chat API
"""

import requests
import json
import time
from typing import Dict, Any

class ChatAPITester:
    def __init__(self, base_url: str = "http://localhost:8000"):
        self.base_url = base_url
        self.chat_url = f"{base_url}/api/v1/chat"
        
    def test_rag_status(self) -> Dict[str, Any]:
        """Test RAG system status"""
        try:
            response = requests.get(f"{self.chat_url}/rag/status")
            print(f"RAG Status Response: {response.status_code}")
            if response.status_code == 200:
                return response.json()
            else:
                print(f"Error: {response.text}")
                return {}
        except Exception as e:
            print(f"Error testing RAG status: {e}")
            return {}
    
    def send_message(self, message: str, user_id: str = "test_user", session_id: str = None) -> Dict[str, Any]:
        """Send message to chat API"""
        try:
            payload = {
                "message": message,
                "user_id": user_id,
                "session_id": session_id
            }
            
            response = requests.post(self.chat_url, json=payload)
            print(f"Chat Response: {response.status_code}")
            
            if response.status_code == 200:
                return response.json()
            else:
                print(f"Error: {response.text}")
                return {}
                
        except Exception as e:
            print(f"Error sending message: {e}")
            return {}
    
    def get_chat_history(self, session_id: str) -> Dict[str, Any]:
        """Get chat history"""
        try:
            response = requests.get(f"{self.chat_url}/sessions/{session_id}/history")
            print(f"History Response: {response.status_code}")
            
            if response.status_code == 200:
                return response.json()
            else:
                print(f"Error: {response.text}")
                return {}
                
        except Exception as e:
            print(f"Error getting history: {e}")
            return {}
    
    def rebuild_vector_store(self) -> Dict[str, Any]:
        """Rebuild vector store"""
        try:
            response = requests.post(f"{self.chat_url}/rag/rebuild")
            print(f"Rebuild Response: {response.status_code}")
            
            if response.status_code == 200:
                return response.json()
            else:
                print(f"Error: {response.text}")
                return {}
                
        except Exception as e:
            print(f"Error rebuilding vector store: {e}")
            return {}
    
    def run_comprehensive_test(self):
        """Run comprehensive test suite"""
        print("=" * 60)
        print("🧪 RAG CHAT API COMPREHENSIVE TEST")
        print("=" * 60)
        
        # Test 1: Check RAG status
        print("\n1️⃣ Testing RAG Status...")
        status = self.test_rag_status()
        if status:
            print(f"✅ RAG Status: {json.dumps(status, indent=2, ensure_ascii=False)}")
        else:
            print("❌ Failed to get RAG status")
        
        # Test 2: Send kidney-related questions
        print("\n2️⃣ Testing Kidney-related Questions...")
        kidney_questions = [
            "Bệnh thận mạn tính là gì?",
            "Các triệu chứng của bệnh thận?",
            "Cách phòng ngừa bệnh thận?",
            "Điều trị bệnh thận như thế nào?",
            "Chế độ ăn cho người bệnh thận?"
        ]
        
        session_id = f"test_session_{int(time.time())}"
        
        for i, question in enumerate(kidney_questions, 1):
            print(f"\n📝 Question {i}: {question}")
            response = self.send_message(question, session_id=session_id)
            
            if response:
                print(f"🤖 Response: {response.get('response', 'No response')}")
                print(f"📊 Confidence: {response.get('confidence', 'N/A')}")
                print(f"🔍 RAG Response: {response.get('is_rag_response', False)}")
                print(f"📚 Sources: {response.get('num_sources', 0)}")
                
                if response.get('sources'):
                    print("📖 Source Files:")
                    for source in response['sources'][:3]:  # Show first 3 sources
                        print(f"   - {source.get('file', 'Unknown')} (Page {source.get('page', 'N/A')})")
            else:
                print("❌ Failed to get response")
            
            time.sleep(1)  # Small delay between requests
        
        # Test 3: Get chat history
        print(f"\n3️⃣ Testing Chat History for session: {session_id}")
        history = self.get_chat_history(session_id)
        if history:
            print(f"📝 History entries: {len(history.get('history', []))}")
            # Show last 2 entries
            for entry in history.get('history', [])[-2:]:
                print(f"   👤 {entry.get('type', 'unknown')}: {entry.get('message', 'N/A')[:100]}...")
        else:
            print("❌ Failed to get chat history")
        
        # Test 4: Test non-kidney questions (fallback)
        print("\n4️⃣ Testing Non-kidney Questions (Fallback)...")
        general_questions = [
            "Hôm nay thời tiết thế nào?",
            "Cách nấu ăn ngon?",
            "What is the weather today?"
        ]
        
        for question in general_questions:
            print(f"\n📝 Question: {question}")
            response = self.send_message(question, session_id=session_id)
            
            if response:
                print(f"🤖 Response: {response.get('response', 'No response')[:200]}...")
                print(f"🔍 RAG Response: {response.get('is_rag_response', False)}")
            
            time.sleep(0.5)
        
        print("\n" + "=" * 60)
        print("✅ TEST COMPLETED")
        print("=" * 60)


def main():
    """Main function"""
    print("🚀 Starting RAG Chat API Test...")
    
    # Check if server is running
    try:
        response = requests.get("http://localhost:8000")
        if response.status_code != 200:
            print("❌ Server not responding. Make sure the API server is running on port 8000")
            print("Run: python main.py")
            return
    except Exception as e:
        print(f"❌ Cannot connect to server: {e}")
        print("Make sure the API server is running on port 8000")
        return
    
    # Run tests
    tester = ChatAPITester()
    tester.run_comprehensive_test()


if __name__ == "__main__":
    main()
