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
from langchain_huggingface import HuggingFaceEmbeddings
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
        
        # Khởi tạo embeddings model (ưu tiên dùng GPU nếu có, fallback về CPU)
        import torch
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
        
        # Thử model nhẹ hơn trước cho GPU có ít RAM
        model_name = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
        
        try:
            # print(f"🔧 Đang thử embedding model: {model_name} (device={device})")
            self.embeddings = HuggingFaceEmbeddings(
                model_name=model_name,
                model_kwargs={'device': device}
            )
            # print(f"✅ Thành công với {model_name} trên {device}")
        except Exception as cuda_error:
            if "CUDA out of memory" in str(cuda_error) and device == 'cuda':
                # print(f"⚠️  GPU hết bộ nhớ, chuyển về CPU: {cuda_error}")
                try:
                    self.embeddings = HuggingFaceEmbeddings(
                        model_name=model_name,
                        model_kwargs={'device': 'cpu'}
                    )
                    # print(f"✅ Thành công với {model_name} trên CPU")
                except Exception as cpu_error:
                    # print(f"❌ Lỗi trên CPU, thử model nhỏ hơn: {cpu_error}")
                    # Fallback về model rất nhỏ
                    model_name = "sentence-transformers/paraphrase-multilingual-MiniLM-L6-v2"
                    self.embeddings = HuggingFaceEmbeddings(
                        model_name=model_name,
                        model_kwargs={'device': 'cpu'}
                    )
                    # print(f"✅ Thành công với model nhỏ: {model_name} trên CPU")
            else:
                raise cuda_error

        # Text splitter
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap,
            length_function=len,
            separators=["\n\n", "\n", " ", ""]
        )
        
        # Khởi tạo các components
        self.vector_store = None
        self.retriever = None
        self.qa_chain = None
        
        # Tạo thư mục vector store nếu chưa có
        self.vector_store_path.mkdir(parents=True, exist_ok=True)
    
    def load_pdfs(self) -> List[Document]:
        """
        Đọc tất cả file PDF trong thư mục
        
        Returns:
            List[Document]: Danh sách documents
        """
        documents = []
        pdf_files = list(self.pdf_directory.glob("*.pdf"))
        
        if not pdf_files:
            # print(f"❌ Không tìm thấy file PDF nào trong: {self.pdf_directory}")
            return documents
        
        # print(f"📚 Tìm thấy {len(pdf_files)} file PDF:")
        for pdf_file in pdf_files:
            pass  # print(f"   - {pdf_file.name}")
        
        for pdf_file in pdf_files:
            try:
                # print(f"📖 Đang đọc: {pdf_file.name}")
                loader = PyPDFLoader(str(pdf_file))
                docs = loader.load()
                
                # Thêm metadata
                for doc in docs:
                    doc.metadata['source_file'] = pdf_file.name
                
                documents.extend(docs)
                # print(f"   ✅ Đã đọc {len(docs)} trang")
            except Exception as e:
                pass  # print(f"   ❌ Lỗi khi đọc {pdf_file.name}: {str(e)}")
        
        # print(f"📄 Tổng cộng: {len(documents)} trang từ {len(pdf_files)} file")
        return documents
    
    def create_chunks(self, documents: List[Document]) -> List[Document]:
        """
        Chia documents thành chunks nhỏ hơn
        
        Args:
            documents: Danh sách documents gốc
            
        Returns:
            List[Document]: Danh sách chunks
        """
        if not documents:
            return []
        
        # print(f"✂️  Đang chia thành chunks (size={self.chunk_size}, overlap={self.chunk_overlap})...")
        chunks = self.text_splitter.split_documents(documents)
        # print(f"📝 Đã tạo {len(chunks)} chunks")
        return chunks
    
    def create_vector_store(self, chunks: List[Document]) -> FAISS:
        """
        Tạo FAISS vector store từ chunks
        
        Args:
            chunks: Danh sách text chunks
            
        Returns:
            FAISS: Vector store
        """
        if not chunks:
            raise ValueError("Không có chunks để tạo vector store")
        
        # print(f"🔮 Đang tạo embeddings cho {len(chunks)} chunks...")
        try:
            vector_store = FAISS.from_documents(chunks, self.embeddings)
            # print(f"✅ Đã tạo vector store với {vector_store.index.ntotal} vectors")
            return vector_store
        except Exception as e:
            # print(f"❌ Lỗi khi tạo vector store: {str(e)}")
            raise
    
    def save_vector_store(self):
        """Lưu vector store ra file"""
        if self.vector_store is None:
            # print("❌ Không có vector store để lưu")
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
            
            # print(f"💾 Vector store đã được lưu tại: {self.vector_store_path}")
        except Exception as e:
            pass  # print(f"❌ Lỗi khi lưu vector store: {str(e)}")
    
    def load_vector_store(self) -> bool:
        """
        Tải vector store đã lưu
        
        Returns:
            bool: True nếu tải thành công
        """
        index_path = self.vector_store_path / "faiss_index"
        metadata_path = self.vector_store_path / "metadata.pkl"
        
        if not index_path.exists() or not metadata_path.exists():
            # print("📁 Không tìm thấy vector store đã lưu")
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
            
            # print(f"📂 Đã tải vector store với {self.vector_store.index.ntotal} vectors")
            return True
        except Exception as e:
            # print(f"❌ Lỗi khi tải vector store: {str(e)}")
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
        # print(f"🔍 Retriever đã được thiết lập (k={k})")
    
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
            formatted_docs.append(f"{doc.page_content}")
        
        return "\n\n".join(formatted_docs)
    
    def create_qa_chain(self):
        """Tạo QA chain với RAG"""
        # Prompt template
        prompt_template = ChatPromptTemplate.from_template("""
Bạn là một chuyên gia y tế về bệnh thận. Hãy trả lời câu hỏi dựa trên các tài liệu được cung cấp.

NGUYÊN TẮC TRẢ LỜI:
1. Chỉ sử dụng thông tin từ các tài liệu được cung cấp
2. Nếu không tìm thấy thông tin liên quan, hãy nói rõ
3. Trả lời bằng tiếng Việt, rõ ràng và dễ hiểu
4. Đưa ra câu trả lời trực tiếp mà không cần nêu nguồn tài liệu
5. Nếu có nhiều quan điểm, hãy trình bày đầy đủ
6. **QUAN TRỌNG**: Trả lời theo định dạng Markdown với:
   - Sử dụng **text** cho phần chữ đậm quan trọng
   - Sử dụng *text* cho phần chữ nghiêng nhấn mạnh
   - Sử dụng ## cho tiêu đề chính
   - Sử dụng ### cho tiêu đề phụ
   - Sử dụng - hoặc * cho danh sách
   - Sử dụng 1. 2. 3. cho danh sách có số thứ tự
   - Sử dụng `code` cho thuật ngữ y khoa chính xác
   - Sử dụng > cho phần trích dẫn quan trọng

CÂU HỎI: {question}

TÀI LIỆU THAM KHẢO:
{context}

TRẢ LỜI (theo định dạng Markdown):
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
    
    def _get_general_ai_response(self, question: str) -> str:
        """
        Sử dụng kiến thức chung của AI khi không tìm thấy thông tin trong tài liệu
        
        Args:
            question: Câu hỏi của người dùng
            
        Returns:
            str: Câu trả lời từ kiến thức chung của AI
        """
        try:
            general_prompt = f"""
Bạn là một chuyên gia y tế về bệnh thận. Câu hỏi sau không tìm thấy thông tin cụ thể trong tài liệu tham khảo.

Hãy trả lời dựa trên kiến thức y khoa chung của bạn về bệnh thận, với những nguyên tắc sau:
1. Trả lời ngắn gọn, súc tích (tối đa 3-4 câu)
2. Sử dụng tiếng Việt
3. Đưa ra thông tin cơ bản và đáng tin cậy
4. Nếu là vấn đề nghiêm trọng, khuyên nên tham khảo bác sĩ
5. Bắt đầu bằng "**Dựa trên kiến thức y khoa chung:**"
6. **QUAN TRỌNG**: Trả lời theo định dạng Markdown với:
   - Sử dụng **text** cho phần chữ đậm quan trọng
   - Sử dụng *text* cho phần chữ nghiêng nhấn mạnh
   - Sử dụng - hoặc * cho danh sách
   - Sử dụng `thuật ngữ` cho thuật ngữ y khoa
   - Sử dụng > cho phần cảnh báo quan trọng

Câu hỏi: {question}

Trả lời (theo định dạng Markdown):
"""
            
            response = self.model.generate_content(general_prompt)
            return response.text
            
        except Exception as e:
            return f"Xin lỗi, tôi không thể trả lời câu hỏi này. Vui lòng tham khảo ý kiến bác sĩ chuyên khoa."

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
                pass  # print("✅ Sử dụng vector store đã có")
            else:
                pass  # print("🔄 Tạo mới vector store...")
                
                # Đọc PDF files
                documents = self.load_pdfs()
                if not documents:
                    pass  # print("❌ Không có documents để xử lý")
                    return False
                
                # Tạo chunks
                chunks = self.create_chunks(documents)
                if not chunks:
                    pass  # print("❌ Không tạo được chunks")
                    return False
                
                # Tạo vector store
                self.vector_store = self.create_vector_store(chunks)
                
                # Lưu vector store
                self.save_vector_store()
            
            # Setup retriever
            self.setup_retriever()
            
            # Tạo QA chain
            self.qa_chain = self.create_qa_chain()
            
            # print("🎉 Hệ thống RAG đã sẵn sàng!")
            return True
            
        except Exception as e:
            # print(f"❌ Lỗi khởi tạo hệ thống: {str(e)}")
            return False
    
    def ask_question(self, question: str) -> Dict[str, Any]:
        """
        Đặt câu hỏi cho chatbot
        
        Args:
            question: Câu hỏi của người dùng
            
        Returns:
            Dict: Kết quả chỉ bao gồm response và confidence
        """
        if not hasattr(self, 'qa_chain'):
            raise ValueError("Hệ thống chưa được khởi tạo. Hãy gọi initialize_system() trước.")
        
        try:
            # Lấy relevant documents
            relevant_docs = self.retriever.get_relevant_documents(question)
            
            # Kiểm tra xem có documents liên quan không
            if not relevant_docs or len(relevant_docs) == 0:
                # Không tìm thấy tài liệu liên quan, sử dụng kiến thức chung của AI
                general_answer = self._get_general_ai_response(question)
                return {
                    'response': general_answer,
                    'confidence': 0.3  # Confidence thấp hơn vì không dựa trên tài liệu
                }
            
            # Tính confidence dựa trên số lượng documents tìm được
            confidence = self._calculate_confidence(question, relevant_docs)
            
            # Nếu confidence quá thấp (< 0.2), cũng fallback sang general AI
            if confidence < 0.2:
                general_answer = self._get_general_ai_response(question)
                return {
                    'response': general_answer,
                    'confidence': 0.3
                }
            
            # Tạo answer từ RAG
            answer = self.qa_chain.invoke(question)
            
            # Kiểm tra xem câu trả lời có chứa các cụm từ "không tìm thấy" hay không
            no_info_phrases = [
                "không tìm thấy thông tin",
                "không có thông tin",
                "không được đề cập",
                "không có dữ liệu",
                "không tìm được",
                "không được cung cấp"
            ]
            
            answer_lower = answer.lower()
            if any(phrase in answer_lower for phrase in no_info_phrases):
                # RAG answer cho biết không có thông tin, fallback sang general AI
                general_answer = self._get_general_ai_response(question)
                return {
                    'response': general_answer,
                    'confidence': 0.3
                }
            
            return {
                'response': answer,
                'confidence': confidence
            }
        
        except Exception as e:
            return {
                'response': f"Lỗi khi xử lý câu hỏi: {str(e)}",
                'confidence': 0.0
            }
    
    def _calculate_confidence(self, question: str, relevant_docs: List[Document]) -> float:
        """
        Tính toán confidence score dựa trên số lượng documents và độ liên quan
        
        Args:
            question: Câu hỏi của người dùng
            relevant_docs: Danh sách documents liên quan
            
        Returns:
            float: Confidence score từ 0.0 đến 1.0
        """
        if not relevant_docs:
            return 0.0
        
        # Base confidence dựa trên số lượng documents tìm được
        num_docs = len(relevant_docs)
        base_confidence = min(num_docs / 5.0, 1.0)  # Tối đa 1.0 khi có >=5 docs
        
        # Kiểm tra độ dài của documents (documents dài hơn thường chứa nhiều thông tin hơn)
        avg_doc_length = sum(len(doc.page_content) for doc in relevant_docs) / num_docs
        length_factor = min(avg_doc_length / 1000.0, 1.0)  # Chuẩn hóa theo 1000 ký tự
        
        # Confidence cuối cùng (trọng số: 70% base, 30% length)
        final_confidence = (base_confidence * 0.7) + (length_factor * 0.3)
        
        return round(final_confidence, 2)
    
    def start_chat_session(self):
        """Bắt đầu phiên chat tương tác"""
        # print("\n" + "="*60)
        # print("🤖 RAG PDF CHATBOT - CHUYÊN GIA BỆNH THẬN")
        # print("="*60)
        # print("💡 Hướng dẫn:")
        # print("   - Gõ câu hỏi và nhấn Enter")
        # print("   - Gõ 'quit' hoặc 'exit' để thoát")
        # print("   - Gõ 'rebuild' để tái tạo vector store")
        # print("="*60)
        
        while True:
            try:
                question = input("\n❓ Câu hỏi của bạn: ").strip()
                
                if question.lower() in ['quit', 'exit', 'q']:
                    # print("👋 Cảm ơn bạn đã sử dụng RAG PDF Chatbot!")
                    break
                
                if question.lower() == 'rebuild':
                    # print("🔄 Đang tái tạo vector store...")
                    if self.initialize_system(force_rebuild=True):
                        pass  # print("✅ Đã tái tạo thành công!")
                    else:
                        pass  # print("❌ Lỗi khi tái tạo vector store")
                    continue
                
                if not question:
                    pass  # print("⚠️  Vui lòng nhập câu hỏi")
                    continue
                
                # print("\n🔍 Đang tìm kiếm thông tin liên quan...")
                result = self.ask_question(question)
                
                # print(f"\n💬 **Trả lời (Markdown format):**")
                # print(result['response'])
                
                # Hiển thị nguồn thông tin
                if result['confidence'] >= 0.4:
                    pass  # print(f"\n📊 **Độ tin cậy:** {result['confidence']:.0%} (Từ tài liệu PDF)")
                else:
                    pass  # print(f"\n📊 **Độ tin cậy:** {result['confidence']:.0%} (Từ kiến thức chung)")
                
            except KeyboardInterrupt:
                pass  # print("\n\n👋 Cảm ơn bạn đã sử dụng RAG PDF Chatbot!")
                break
            except Exception as e:
                pass  # print(f"\n❌ Lỗi: {str(e)}")


def main():
    """Hàm main để chạy chatbot"""
    # print("🚀 Khởi động RAG PDF Chatbot...")
    
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
            # print("❌ Không thể khởi tạo hệ thống. Vui lòng kiểm tra:")
            # print("   1. Đã đặt file PDF vào thư mục data/")
            # print("   2. Đã cài đặt đầy đủ thư viện")
            # print("   3. Đã cung cấp GEMINI_API_KEY")
            return
        
        # Bắt đầu chat session
        chatbot.start_chat_session()
        
    except Exception as e:
        pass  # print(f"❌ Lỗi khởi tạo: {str(e)}")


if __name__ == "__main__":
    main()
