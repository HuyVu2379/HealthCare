"""
RAG PDF Chatbot using LangChain, FAISS, and Google Gemini
Hệ thống chatbot trả lời câu hỏi dựa trên nội dung các file PDF về bệnh thận
"""

import os
import sys
import pickle
from typing import List, Dict, Any, Optional
from pathlib import Path
import google.generativeai as genai
from dotenv import load_dotenv

# LangChain imports
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import PyPDFLoader
from langchain_community.vectorstores import FAISS
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain.schema import Document
from langchain.prompts import ChatPromptTemplate
from langchain.schema.runnable import RunnablePassthrough
from langchain.schema.output_parser import StrOutputParser

# Load environment variables
load_dotenv()

class RAGPDFChatbot:
    """
    RAG Chatbot để trả lời câu hỏi dựa trên nội dung PDF
    Sử dụng FAISS để lưu trữ vector và Google Gemini làm LLM
    """
    
    def __init__(self, 
                 pdf_directory: str = "./data",
                 vector_store_path: str = "./vector_store",
                 gemini_api_key: Optional[str] = None,
                 chunk_size: int = 1000,
                 chunk_overlap: int = 200):
        """
        Khởi tạo RAG Chatbot
        
        Args:
            pdf_directory: Thư mục chứa file PDF
            vector_store_path: Đường dẫn lưu FAISS vector store
            gemini_api_key: API key cho Google Gemini
            chunk_size: Kích thước mỗi chunk text
            chunk_overlap: Số ký tự overlap giữa các chunk
        """
        self.pdf_directory = Path(pdf_directory)
        self.vector_store_path = Path(vector_store_path)
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap
        
        # Thiết lập Google Gemini
        self.gemini_api_key = gemini_api_key or os.getenv("GEMINI_API_KEY")
        if not self.gemini_api_key:
            raise ValueError("Vui lòng cung cấp GEMINI_API_KEY trong file .env hoặc tham số")
        
        genai.configure(api_key=self.gemini_api_key)
        self.model = genai.GenerativeModel('gemini-2.0-flash-exp')
        # self.model = genai.GenerativeModel('gemini-2.5-pro')
        # self.model = genai.GenerativeModel('gemini-2.5-flash')
        
        # Khởi tạo embeddings model (sử dụng model local để tiết kiệm chi phí)
        self.embeddings = HuggingFaceEmbeddings(
            # model_name="sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
            model_name="intfloat/multilingual-e5-large",
            model_kwargs={'device': 'cpu'}
        )
        
        # Text splitter
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap,
            length_function=len,
            separators=["\n\n", "\n", ". ", " ", ""]
        )
        
        # Vector store
        self.vector_store = None
        self.retriever = None
        
        # Tạo thư mục nếu chưa tồn tại
        self.pdf_directory.mkdir(exist_ok=True)
        self.vector_store_path.mkdir(exist_ok=True)
        
        print("✅ RAG PDF Chatbot đã được khởi tạo thành công!")
    
    def load_pdfs(self) -> List[Document]:
        """
        Đọc tất cả file PDF trong thư mục
        
        Returns:
            List[Document]: Danh sách documents từ PDF
        """
        documents = []
        pdf_files = list(self.pdf_directory.glob("*.pdf"))
        
        if not pdf_files:
            print(f"⚠️  Không tìm thấy file PDF nào trong thư mục {self.pdf_directory}")
            return documents
        
        print(f"📚 Đang đọc {len(pdf_files)} file PDF...")
        
        for pdf_file in pdf_files:
            try:
                print(f"   📄 Đang đọc: {pdf_file.name}")
                loader = PyPDFLoader(str(pdf_file))
                pdf_docs = loader.load()
                
                # Thêm metadata về source file
                for doc in pdf_docs:
                    doc.metadata['source_file'] = pdf_file.name
                    doc.metadata['file_path'] = str(pdf_file)
                
                documents.extend(pdf_docs)
                print(f"   ✅ Đã đọc {len(pdf_docs)} trang từ {pdf_file.name}")
                
            except Exception as e:
                print(f"   ❌ Lỗi khi đọc {pdf_file.name}: {str(e)}")
        
        print(f"📖 Tổng cộng đã đọc {len(documents)} trang từ {len(pdf_files)} file PDF")
        return documents
    
    def create_chunks(self, documents: List[Document]) -> List[Document]:
        """
        Chia nhỏ documents thành các chunks
        
        Args:
            documents: List documents từ PDF
            
        Returns:
            List[Document]: List chunks
        """
        print("✂️  Đang chia nhỏ documents thành chunks...")
        
        chunks = self.text_splitter.split_documents(documents)
        
        # Thêm chunk index vào metadata
        for i, chunk in enumerate(chunks):
            chunk.metadata['chunk_id'] = i
            chunk.metadata['chunk_size'] = len(chunk.page_content)
        
        print(f"📝 Đã tạo {len(chunks)} chunks")
        return chunks
    
    def create_vector_store(self, chunks: List[Document]) -> FAISS:
        """
        Tạo vector store từ chunks
        
        Args:
            chunks: List chunks để tạo embeddings
            
        Returns:
            FAISS: Vector store
        """
        print("🔍 Đang tạo embeddings và vector store...")
        
        try:
            vector_store = FAISS.from_documents(chunks, self.embeddings)
            print("✅ Vector store đã được tạo thành công!")
            return vector_store
        except Exception as e:
            print(f"❌ Lỗi khi tạo vector store: {str(e)}")
            raise e
    
    def save_vector_store(self):
        """Lưu vector store để tái sử dụng"""
        if self.vector_store is None:
            print("⚠️  Chưa có vector store để lưu")
            return
        
        try:
            # Lưu FAISS index
            index_path = self.vector_store_path / "faiss_index"
            self.vector_store.save_local(str(index_path))
            
            # Lưu metadata
            metadata_path = self.vector_store_path / "metadata.pkl"
            with open(metadata_path, 'wb') as f:
                pickle.dump({
                    'chunk_size': self.chunk_size,
                    'chunk_overlap': self.chunk_overlap,
                    'embedding_model': self.embeddings.model_name
                }, f)
            
            print(f"💾 Vector store đã được lưu tại: {self.vector_store_path}")
        except Exception as e:
            print(f"❌ Lỗi khi lưu vector store: {str(e)}")
    
    def load_vector_store(self) -> bool:
        """
        Tải vector store đã lưu
        
        Returns:
            bool: True nếu tải thành công
        """
        index_path = self.vector_store_path / "faiss_index"
        metadata_path = self.vector_store_path / "metadata.pkl"
        
        if not index_path.exists() or not metadata_path.exists():
            print("📁 Không tìm thấy vector store đã lưu")
            return False
        
        try:
            # Tải FAISS index
            self.vector_store = FAISS.load_local(
                str(index_path), 
                self.embeddings,
                allow_dangerous_deserialization=True
            )
            
            # Tải metadata
            with open(metadata_path, 'rb') as f:
                metadata = pickle.load(f)
            
            print(f"📂 Đã tải vector store với {self.vector_store.index.ntotal} vectors")
            return True
        except Exception as e:
            print(f"❌ Lỗi khi tải vector store: {str(e)}")
            return False
    
    def setup_retriever(self, k: int = 5):
        """
        Thiết lập retriever từ vector store
        
        Args:
            k: Số lượng chunks liên quan nhất để retrieve
        """
        if self.vector_store is None:
            raise ValueError("Vector store chưa được tạo hoặc tải")
        
        self.retriever = self.vector_store.as_retriever(
            search_type="similarity",
            search_kwargs={"k": k}
        )
        print(f"🔍 Retriever đã được thiết lập (k={k})")
    
    def format_docs(self, docs: List[Document]) -> str:
        """
        Format documents thành string để đưa vào prompt
        
        Args:
            docs: List documents được retrieve
            
        Returns:
            str: Formatted text
        """
        formatted_docs = []
        for i, doc in enumerate(docs, 1):
            source = doc.metadata.get('source_file', 'Unknown')
            page = doc.metadata.get('page', 'Unknown')
            
            formatted_docs.append(
                f"[Tài liệu {i}] (Nguồn: {source}, Trang: {page})\n"
                f"{doc.page_content}\n"
            )
        
        return "\n".join(formatted_docs)
    
    def create_qa_chain(self):
        """Tạo QA chain với RAG"""
        # Prompt template
        prompt_template = ChatPromptTemplate.from_template("""
Bạn là một chuyên gia y tế về bệnh thận. Hãy trả lời câu hỏi dựa trên các tài liệu được cung cấp.

NGUYÊN TẮC TRẢ LỜI:
1. Chỉ sử dụng thông tin từ các tài liệu được cung cấp
2. Nếu không tìm thấy thông tin liên quan, hãy nói rõ
3. Trả lời bằng tiếng Việt, rõ ràng và dễ hiểu
4. Cung cấp nguồn tham chiếu cụ thể
5. Nếu có nhiều quan điểm, hãy trình bày đầy đủ

CÂU HỎI: {question}

TÀI LIỆU THAM KHẢO:
{context}

TRẢ LỜI:
""")
        
        # Tạo chain
        qa_chain = (
            {
                "context": self.retriever | self.format_docs,
                "question": RunnablePassthrough()
            }
            | prompt_template
            | self._call_gemini
            | StrOutputParser()
        )
        
        return qa_chain
    
    def _call_gemini(self, prompt_value):
        """Gọi Google Gemini API"""
        try:
            # Chuyển prompt thành string
            if hasattr(prompt_value, 'to_string'):
                prompt_text = prompt_value.to_string()
            else:
                prompt_text = str(prompt_value)
            
            # Gọi Gemini
            response = self.model.generate_content(prompt_text)
            return response.text
        except Exception as e:
            return f"Lỗi khi gọi Gemini API: {str(e)}"
    
    def initialize_system(self, force_rebuild: bool = False) -> bool:
        """
        Khởi tạo toàn bộ hệ thống RAG
        
        Args:
            force_rebuild: Bắt buộc rebuild vector store
            
        Returns:
            bool: True nếu khởi tạo thành công
        """
        try:
            # Kiểm tra xem có vector store đã lưu không
            if not force_rebuild and self.load_vector_store():
                print("✅ Sử dụng vector store đã có")
            else:
                print("🔄 Tạo mới vector store...")
                
                # Đọc PDF files
                documents = self.load_pdfs()
                if not documents:
                    print("❌ Không có documents để xử lý")
                    return False
                
                # Tạo chunks
                chunks = self.create_chunks(documents)
                if not chunks:
                    print("❌ Không tạo được chunks")
                    return False
                
                # Tạo vector store
                self.vector_store = self.create_vector_store(chunks)
                
                # Lưu vector store
                self.save_vector_store()
            
            # Setup retriever
            self.setup_retriever()
            
            # Tạo QA chain
            self.qa_chain = self.create_qa_chain()
            
            print("🎉 Hệ thống RAG đã sẵn sàng!")
            return True
            
        except Exception as e:
            print(f"❌ Lỗi khởi tạo hệ thống: {str(e)}")
            return False
    
    def ask_question(self, question: str) -> Dict[str, Any]:
        """
        Đặt câu hỏi cho chatbot
        
        Args:
            question: Câu hỏi của người dùng
            
        Returns:
            Dict: Kết quả bao gồm answer và metadata
        """
        if not hasattr(self, 'qa_chain'):
            raise ValueError("Hệ thống chưa được khởi tạo. Hãy gọi initialize_system() trước.")
        
        try:
            # Lấy relevant documents
            relevant_docs = self.retriever.get_relevant_documents(question)
            
            # Tạo answer
            answer = self.qa_chain.invoke(question)
            
            # Chuẩn bị metadata về sources
            sources = []
            for doc in relevant_docs:
                sources.append({
                    'file': doc.metadata.get('source_file', 'Unknown'),
                    'page': doc.metadata.get('page', 'Unknown'),
                    'content_preview': doc.page_content[:200] + "..." if len(doc.page_content) > 200 else doc.page_content
                })
            
            return {
                'question': question,
                'answer': answer,
                'sources': sources,
                'num_sources': len(sources)
            }
        
        except Exception as e:
            return {
                'question': question,
                'answer': f"Lỗi khi xử lý câu hỏi: {str(e)}",
                'sources': [],
                'num_sources': 0
            }
    
    def start_chat_session(self):
        """Bắt đầu phiên chat tương tác"""
        print("\n" + "="*60)
        print("🤖 RAG PDF CHATBOT - CHUYÊN GIA BỆNH THẬN")
        print("="*60)
        print("💡 Hướng dẫn:")
        print("   - Gõ câu hỏi và nhấn Enter")
        print("   - Gõ 'quit' hoặc 'exit' để thoát")
        print("   - Gõ 'rebuild' để tái tạo vector store")
        print("="*60)
        
        while True:
            try:
                question = input("\n❓ Câu hỏi của bạn: ").strip()
                
                if question.lower() in ['quit', 'exit', 'q']:
                    print("👋 Cảm ơn bạn đã sử dụng RAG PDF Chatbot!")
                    break
                
                if question.lower() == 'rebuild':
                    print("🔄 Đang tái tạo vector store...")
                    if self.initialize_system(force_rebuild=True):
                        print("✅ Đã tái tạo thành công!")
                    else:
                        print("❌ Lỗi khi tái tạo vector store")
                    continue
                
                if not question:
                    print("⚠️  Vui lòng nhập câu hỏi")
                    continue
                
                print("\n🔍 Đang tìm kiếm thông tin liên quan...")
                result = self.ask_question(question)
                
                print(f"\n💬 **Trả lời:**")
                print(result['answer'])
                
                if result['sources']:
                    print(f"\n📚 **Nguồn tham khảo ({result['num_sources']} tài liệu):**")
                    for i, source in enumerate(result['sources'], 1):
                        print(f"   {i}. {source['file']} (Trang {source['page']})")
                
            except KeyboardInterrupt:
                print("\n\n👋 Cảm ơn bạn đã sử dụng RAG PDF Chatbot!")
                break
            except Exception as e:
                print(f"\n❌ Lỗi: {str(e)}")


def main():
    """Hàm main để chạy chatbot"""
    print("🚀 Khởi động RAG PDF Chatbot...")
    
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
            print("❌ Không thể khởi tạo hệ thống. Vui lòng kiểm tra:")
            print("   1. Đã đặt file PDF vào thư mục data/")
            print("   2. Đã cài đặt đầy đủ thư viện")
            print("   3. Đã cung cấp GEMINI_API_KEY")
            return
        
        # Bắt đầu chat session
        chatbot.start_chat_session()
        
    except Exception as e:
        print(f"❌ Lỗi khởi tạo: {str(e)}")


if __name__ == "__main__":
    main()
