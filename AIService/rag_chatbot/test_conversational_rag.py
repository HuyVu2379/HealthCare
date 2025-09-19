"""
Demo script để test RAG PDF Chatbot với ConversationalRetrievalChain
Test khả năng ghi nhớ hội thoại và async operations
"""

import asyncio
from pathlib import Path
from rag_pdf_chatbot import RAGPDFChatbot

def test_conversation_memory():
    """Test khả năng ghi nhớ hội thoại"""
    print("🧪 Test ConversationalRetrievalChain với Memory")
    print("="*50)
    
    # Thiết lập đường dẫn
    current_dir = Path(__file__).parent
    pdf_dir = current_dir / "data"
    vector_store_dir = current_dir / "vector_store"
    
    try:
        # Khởi tạo chatbot
        chatbot = RAGPDFChatbot(
            pdf_directory=str(pdf_dir),
            vector_store_path=str(vector_store_dir),
            chunk_size=1000,
            chunk_overlap=200
        )
        
        # Khởi tạo hệ thống
        if not chatbot.initialize_system():
            print("❌ Không thể khởi tạo hệ thống")
            return
        
        print("✅ Hệ thống đã sẵn sàng!\n")
        
        # Test conversation flow
        questions = [
            "Bệnh thận mãn tính là gì?",
            "Có những giai đoạn nào của bệnh này?",
            "Điều trị như thế nào cho giai đoạn đầu?",
            "Tôi có thể ngăn ngừa tiến triển của bệnh không?"
        ]
        
        print("🔄 Test chuỗi câu hỏi liên quan:")
        for i, question in enumerate(questions, 1):
            print(f"\n--- Câu hỏi {i} ---")
            print(f"❓ {question}")
            
            result = chatbot.ask_question(question)
            
            print(f"💬 {result['answer'][:200]}...")
            print(f"📊 Sources: {result['num_sources']}, Context: {result['has_context']}")
        
        print(f"\n🧠 Lịch sử hội thoại: {len(chatbot.get_chat_history())} câu hỏi")
        
        # Test clear memory
        print("\n🔄 Test xóa memory...")
        chatbot.clear_memory()
        
        # Test async
        print("\n🚀 Test Async mode...")
        result = asyncio.run(chatbot.ask_question_async("Triệu chứng của bệnh thận mãn tính?"))
        print(f"💬 Async result: {result['answer'][:100]}...")
        
        print("\n✅ Test completed!")
        
    except Exception as e:
        print(f"❌ Lỗi: {str(e)}")

def interactive_demo():
    """Demo tương tác"""
    print("🎮 Interactive Demo Mode")
    print("="*30)
    
    # Thiết lập đường dẫn
    current_dir = Path(__file__).parent
    pdf_dir = current_dir / "data"
    vector_store_dir = current_dir / "vector_store"
    
    try:
        # Khởi tạo chatbot
        chatbot = RAGPDFChatbot(
            pdf_directory=str(pdf_dir),
            vector_store_path=str(vector_store_dir)
        )
        
        # Khởi tạo hệ thống
        if not chatbot.initialize_system():
            print("❌ Không thể khởi tạo hệ thống")
            return
        
        # Bắt đầu chat session
        chatbot.start_chat_session()
        
    except Exception as e:
        print(f"❌ Lỗi: {str(e)}")

if __name__ == "__main__":
    print("🤖 RAG PDF Chatbot - Demo với ConversationalRetrievalChain")
    print("="*60)
    
    mode = input("Chọn mode (1: Auto test, 2: Interactive): ").strip()
    
    if mode == "1":
        test_conversation_memory()
    elif mode == "2":
        interactive_demo()
    else:
        print("Invalid choice!")