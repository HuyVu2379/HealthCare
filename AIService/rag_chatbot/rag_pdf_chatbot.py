"""
RAG PDF Chatbot using LangChain, FAISS, and Google Gemini
Hệ thống chatbot trả lời câu hỏi dựa trên nội dung các file PDF về bệnh thận
"""

import os
import pickle
import re
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

class MedicalTextSplitter(RecursiveCharacterTextSplitter):
    """
    Custom text splitter tối ưu cho văn bản y khoa tiếng Việt
    Giữ nguyên ngữ cảnh quan trọng và xử lý tốt các cấu trúc y khoa
    """
    
    def __init__(self, chunk_size: int = 1000, chunk_overlap: int = 200, **kwargs):
        """
        Khởi tạo Medical Text Splitter với cấu hình tối ưu cho văn bản y khoa
        
        Args:
            chunk_size: Kích thước chunk (1500 để chứa đủ ngữ cảnh y khoa)
            chunk_overlap: Overlap giữa chunks (300 để không mất thông tin giữa các chunk)
        
        Lý do chọn tham số:
        - chunk_size=1500: Đủ lớn để chứa một đoạn y khoa hoàn chỉnh (triệu chứng + giải thích)
        - chunk_overlap=300: Đảm bảo không cắt đứt thông tin giữa các chunk (20% overlap)
        - Separators ưu tiên: Section headers → Paragraphs → Sentences → Words
        """
        # Separators tối ưu cho văn bản y khoa tiếng Việt
        medical_separators = [
            # 1. Section headers và phần chính (Ưu tiên cao nhất)
            "\n## ",  # Markdown headers level 2
            "\n### ",  # Markdown headers level 3
            "\n#### ",  # Markdown headers level 4
            
            # 2. Ranh giới đoạn văn rõ ràng
            "\n\n\n",  # 3+ newlines - ranh giới section lớn
            "\n\n",    # 2 newlines - ranh giới đoạn văn
            
            # 3. Danh sách và điểm liệt kê (quan trọng cho triệu chứng, quy trình)
            "\n- ",     # Bullet points
            "\n• ",     # Bullet points (alternative)
            "\n* ",     # Asterisk bullets
            "\n+ ",     # Plus bullets
            
            # 4. Danh sách có số thứ tự (quy trình điều trị, giai đoạn bệnh)
            "\n1. ",    # Numbered list item 1
            "\n2. ",    # Numbered list item 2
            "\n3. ",    # Numbered list item 3
            
            # 5. Ranh giới câu hoàn chỉnh (tiếng Việt)
            ". ",       # Kết thúc câu + space
            "! ",       # Câu cảm thán
            "? ",       # Câu hỏi
            "。",       # Dấu chấm full-width (nếu có)
            
            # 6. Dấu câu phụ (chỉ khi không còn cách nào khác)
            "; ",       # Dấu chấm phẩy
            ": ",       # Dấu hai chấm (thường đứng trước danh sách)
            "\n",       # Single newline
            
            # 7. Cuối cùng mới cắt theo từ và ký tự
            " ",        # Space - ranh giới từ
            "",         # Ký tự - chỉ dùng khi thật sự cần thiết
        ]
        
        super().__init__(
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap,
            length_function=len,
            separators=medical_separators,
            **kwargs
        )
    
    def split_text(self, text: str) -> List[str]:
        """
        Override split_text để thêm xử lý đặc biệt cho văn bản y khoa
        
        Args:
            text: Văn bản cần chia
            
        Returns:
            List[str]: Danh sách chunks đã được tối ưu
        """
        # Normalize text trước khi split
        text = self._normalize_medical_text(text)
        
        # Gọi split_text gốc
        chunks = super().split_text(text)
        
        # Post-process chunks
        processed_chunks = []
        for chunk in chunks:
            # Loại bỏ whitespace thừa
            chunk = chunk.strip()
            
            # Chỉ giữ chunks có nội dung (>= 50 ký tự)
            if len(chunk) >= 50:
                processed_chunks.append(chunk)
        
        return processed_chunks
    
    def _normalize_medical_text(self, text: str) -> str:
        """
        Chuẩn hóa văn bản y khoa trước khi chunking
        
        Args:
            text: Văn bản gốc
            
        Returns:
            str: Văn bản đã chuẩn hóa
        """
        # 1. Chuẩn hóa multiple spaces thành single space
        text = re.sub(r' +', ' ', text)
        
        # 2. Chuẩn hóa multiple newlines (giữ tối đa 3 newlines)
        text = re.sub(r'\n{4,}', '\n\n\n', text)
        
        # 3. Xóa spaces ở đầu/cuối mỗi dòng
        text = '\n'.join(line.strip() for line in text.split('\n'))
        
        # 4. Đảm bảo có space sau dấu câu (nếu chưa có)
        text = re.sub(r'([.!?])([A-ZĐÂĂÊÔƠƯÁÀẢÃẠ])', r'\1 \2', text)
        
        return text

class RAGPDFChatbot:
    """
    RAG Chatbot để trả lời câu hỏi dựa trên nội dung PDF
    Sử dụng FAISS để lưu trữ vector và Google Gemini làm LLM
    """
    
    def __init__(self, 
                 pdf_directory: str = "./data",
                 vector_store_path: str = "./vector_store",
                 gemini_api_key: Optional[str] = None,
                 chunk_size: int = 1500,
                 chunk_overlap: int = 300):
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
        # self.model = genai.GenerativeModel('gemini-2.5-flash')
    
        # self.model = genai.GenerativeModel('gemini-2.5-pro')
        # Sử dụng gemini-2.5-flash với generation config tối ưu
        self.model = genai.GenerativeModel(
            'gemini-2.5-flash',
            generation_config=genai.GenerationConfig(
                temperature=0.1,  # Giảm temperature để responses nhanh và ổn định hơn
                top_p=0.95,
                top_k=40,
                max_output_tokens=2048,
            )
        )
        
        # Khởi tạo embeddings model (ưu tiên dùng GPU nếu có, fallback về CPU)
        import torch
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
        
        # Thử model nhẹ hơn trước cho GPU có ít RAM
        model_name = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
        
        try:
            self.embeddings = HuggingFaceEmbeddings(
                model_name=model_name,
                model_kwargs={'device': device}
            )
        except Exception as cuda_error:
            if "CUDA out of memory" in str(cuda_error) and device == 'cuda':
                try:
                    self.embeddings = HuggingFaceEmbeddings(
                        model_name=model_name,
                        model_kwargs={'device': 'cpu'}
                    )
                except Exception as cpu_error:
                    # Fallback về model rất nhỏ
                    model_name = "sentence-transformers/paraphrase-multilingual-MiniLM-L6-v2"
                    self.embeddings = HuggingFaceEmbeddings(
                        model_name=model_name,
                        model_kwargs={'device': 'cpu'}
                    )
            else:
                raise cuda_error

        # Text splitter - Sử dụng MedicalTextSplitter tối ưu cho văn bản y khoa
        self.text_splitter = MedicalTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap
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
            return documents
        
        for pdf_file in pdf_files:
            try:
                loader = PyPDFLoader(str(pdf_file))
                docs = loader.load()
                
                # Thêm metadata
                for doc in docs:
                    doc.metadata['source_file'] = pdf_file.name
                
                documents.extend(docs)
            except Exception as e:
                pass
        
        return documents
    
    def create_chunks(self, documents: List[Document]) -> List[Document]:
        """
        Chia documents thành chunks nhỏ hơn với metadata tracking
        
        Args:
            documents: Danh sách documents gốc
            
        Returns:
            List[Document]: Danh sách chunks với metadata đầy đủ
        """
        if not documents:
            return []
        
        chunks = self.text_splitter.split_documents(documents)
        
        # Thêm metadata tracking cho mỗi chunk
        enhanced_chunks = []
        total_chunks = len(chunks)
        
        for i, chunk in enumerate(chunks):
            # Thêm metadata để trace back
            chunk.metadata['chunk_index'] = i + 1
            chunk.metadata['total_chunks'] = total_chunks
            chunk.metadata['chunk_size'] = len(chunk.page_content)
            
            # Detect section type dựa trên content
            chunk.metadata['section_type'] = self._detect_section_type(chunk.page_content)
            
            # Thêm timestamp nếu cần
            chunk.metadata['processed_at'] = str(Path(__file__).stat().st_mtime)
            
            enhanced_chunks.append(chunk)
        
        return enhanced_chunks
    
    def _detect_section_type(self, content: str) -> str:
        """
        Phát hiện loại section dựa trên nội dung chunk
        
        Args:
            content: Nội dung chunk
            
        Returns:
            str: Loại section (symptoms, treatment, diagnosis, general)
        """
        content_lower = content.lower()
        
        # Keywords cho từng loại section
        symptoms_keywords = ['triệu chứng', 'biểu hiện', 'dấu hiệu', 'cảm giác', 'đau', 'mệt']
        treatment_keywords = ['điều trị', 'thuốc', 'liệu pháp', 'phẫu thuật', 'chữa', 'uống']
        diagnosis_keywords = ['chẩn đoán', 'xét nghiệm', 'kiểm tra', 'kết quả', 'chỉ số', 'đo']
        prevention_keywords = ['phòng ngừa', 'dự phòng', 'tránh', 'hạn chế', 'kiêng']
        
        # Đếm số lần xuất hiện của mỗi loại keyword
        scores = {
            'symptoms': sum(1 for kw in symptoms_keywords if kw in content_lower),
            'treatment': sum(1 for kw in treatment_keywords if kw in content_lower),
            'diagnosis': sum(1 for kw in diagnosis_keywords if kw in content_lower),
            'prevention': sum(1 for kw in prevention_keywords if kw in content_lower)
        }
        
        # Trả về loại có điểm cao nhất, hoặc 'general' nếu không có
        max_score = max(scores.values())
        if max_score > 0:
            return max(scores, key=scores.get)
        return 'general'
    
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
        
        try:
            vector_store = FAISS.from_documents(chunks, self.embeddings)
            return vector_store
        except Exception as e:
            raise
    
    def save_vector_store(self):
        """Lưu vector store ra file"""
        if self.vector_store is None:
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
        except Exception as e:
            pass
    
    def load_vector_store(self) -> bool:
        """
        Tải vector store đã lưu
        
        Returns:
            bool: True nếu tải thành công
        """
        index_path = self.vector_store_path / "faiss_index"
        metadata_path = self.vector_store_path / "metadata.pkl"
        
        if not index_path.exists() or not metadata_path.exists():
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
            
            return True
        except Exception as e:
            return False
    
    def setup_retriever(self, k: int = 5):
        """
        Thiết lập retriever từ vector store
        
        Args:
            k: Số lượng chunks liên quan nhất để retrieve (giảm từ 8 xuống 5 để tăng tốc)
        """
        if self.vector_store is None:
            raise ValueError("Vector store chưa được tạo hoặc tải")
        
        # Chuyển sang similarity search (nhanh hơn MMR 2-3 lần mà vẫn chính xác)
        self.retriever = self.vector_store.as_retriever(
            search_type="similarity",
            search_kwargs={
                "k": k  # 5 documents vẫn đủ context cho câu trả lời chính xác
            }
        )
    
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
        # Prompt template siêu chi tiết - buộc AI trả lời đầy đủ
        prompt_template = ChatPromptTemplate.from_template("""
Bạn là **Bác sĩ chuyên khoa Thận - Tiết niệu** với 20 năm kinh nghiệm lâm sàng. Nhiệm vụ của bạn là cung cấp câu trả lời Y KHOA CHI TIẾT, CHÍNH XÁC và DỄ HIỂU dựa HOÀN TOÀN trên các tài liệu tham khảo được cung cấp.

═══════════════════════════════════════════════════════════════════════════════
📋 NGUYÊN TẮC TRẢ LỜI BẮT BUỘC
═══════════════════════════════════════════════════════════════════════════════

✅ **QUY TẮC VÀNG:**
1. **ĐỌC KỸ VÀ TRÍCH XUẤT TẤT CẢ** thông tin liên quan từ tài liệu tham khảo
2. **TRẢ LỜI DÀI VÀ CHI TIẾT** - Tối thiểu 150-250 từ (trừ câu hỏi đơn giản)
3. **KHÔNG BỎ SÓT** bất kỳ thông tin quan trọng nào có trong tài liệu
4. **SỬ DỤNG TẤT CẢ SỐ LIỆU, THỐNG KÊ, CHỈ SỐ** nếu có trong context
5. **GIẢI THÍCH RÕ RÀNG** các thuật ngữ y khoa phức tạp

❌ **CẤM TUYỆT ĐỐI:**
- Trả lời ngắn gọn, sơ sài (< 100 từ)
- Bỏ qua thông tin quan trọng có trong tài liệu
- Thêm thông tin không có trong tài liệu (NO HALLUCINATION)
- Trả lời mơ hồ, chung chung
- Bỏ qua số liệu, thống kê, chỉ số cụ thể

═══════════════════════════════════════════════════════════════════════════════
📐 CẤU TRÚC TRẢ LỜI BẮT BUỘC (MARKDOWN FORMAT)
═══════════════════════════════════════════════════════════════════════════════

Tùy theo loại câu hỏi, hãy sử dụng cấu trúc phù hợp:

🔹 **LOẠI 1: Câu hỏi về BỆNH LÝ / TRIỆU CHỨNG**
Cấu trúc:
## Tổng quan
[Định nghĩa ngắn gọn về bệnh/triệu chứng - 2-3 câu]

### 📊 Nguyên nhân
- [Liệt kê TẤT CẢ nguyên nhân từ tài liệu]
- [Bao gồm cả nguyên nhân chính và nguyên nhân phụ]
- [Kèm số liệu nếu có]

### 🩺 Triệu chứng và Biểu hiện
**Triệu chứng thường gặp:**
- [Chi tiết từng triệu chứng]
- [Mô tả cách biểu hiện]

**Triệu chứng nghiêm trọng:**
- [Các dấu hiệu cần cấp cứu]

### 🔬 Chẩn đoán
- [Các xét nghiệm cần thiết]
- [Chỉ số bình thường/bất thường]
- [Phương pháp chẩn đoán]

### 💊 Điều trị
**Điều trị nội khoa:**
- [Chi tiết về thuốc, liều lượng nếu có]

**Điều trị ngoại khoa:**
- [Phẫu thuật, can thiệp nếu có]

**Thay đổi lối sống:**
- [Chế độ ăn, sinh hoạt]

### ⚠️ Lưu ý quan trọng
- [Các biến chứng có thể xảy ra]
- [Khi nào cần gặp bác sĩ ngay]

---

🔹 **LOẠI 2: Câu hỏi về XÉT NGHIỆM / CHỈ SỐ**
Cấu trúc:
## Giới thiệu về [Tên xét nghiệm]
[Mục đích, ý nghĩa của xét nghiệm]

### 📈 Chỉ số bình thường
| Đối tượng | Chỉ số bình thường | Đơn vị |
|-----------|-------------------|--------|
| [Từ tài liệu] | [Giá trị] | [Đơn vị] |

### 🔴 Chỉ số bất thường
**Khi chỉ số cao:**
- Nguyên nhân: [...]
- Ý nghĩa: [...]
- Cần làm gì: [...]

**Khi chỉ số thấp:**
- Nguyên nhân: [...]
- Ý nghĩa: [...]
- Cần làm gì: [...]

---

🔹 **LOẠI 3: Câu hỏi về ĐIỀU TRỊ / THUỐC**
Cấu trúc:
## Phương pháp điều trị [Tên]
[Giới thiệu tổng quan]

### 💊 Thông tin thuốc (nếu có)
- **Tên thuốc:** [...]
- **Hoạt chất:** [...]
- **Liều lượng:** [...]
- **Cách dùng:** [...]
- **Tác dụng phụ:** [...]

### ✅ Hiệu quả
- [Mô tả chi tiết hiệu quả từ tài liệu]

### ⚠️ Chống chỉ định
- [Các trường hợp không nên dùng]

---

🔹 **LOẠI 4: Câu hỏi CHUNG**
Trả lời chi tiết với:
- Heading (##, ###) phân chia rõ ràng
- Bullet points cho danh sách
- **Bold** cho điểm quan trọng
- *Italic* cho nhấn mạnh
- `Code` cho thuật ngữ y khoa
- > Blockquote cho cảnh báo quan trọng

═══════════════════════════════════════════════════════════════════════════════
🎨 QUY TẮC ĐỊNH DẠNG MARKDOWN (BẮT BUỘC)
═══════════════════════════════════════════════════════════════════════════════

✓ **Heading:** Dùng ## cho tiêu đề chính, ### cho tiêu đề phụ
✓ **Bold:** Dùng **text** cho thông tin CỰC KỲ quan trọng
✓ **Italic:** Dùng *text* cho nhấn mạnh nhẹ
✓ **List:** Dùng - hoặc 1. 2. 3. cho danh sách có cấu trúc
✓ **Code:** Dùng `thuật ngữ` cho tên bệnh, thuốc, chỉ số y khoa
✓ **Quote:** Dùng > cho cảnh báo, lưu ý đặc biệt quan trọng
✓ **Table:** Dùng bảng Markdown cho số liệu, so sánh

VÍ DỤ FORMATTING:
```markdown
## Tiêu đề chính

### Phần con

**Điểm cực kỳ quan trọng** - cần ghi nhớ

*Nhấn mạnh* điều này

- Danh sách item 1
- Danh sách item 2

> ⚠️ **CẢNH BÁO:** Thông tin quan trọng cần chú ý

Chỉ số `GFR < 60 ml/min/1.73m²` cho thấy suy thận mãn tính.
```

═══════════════════════════════════════════════════════════════════════════════
🔍 XỬ LÝ CÁC TÌNH HUỐNG ĐẶC BIỆT
═══════════════════════════════════════════════════════════════════════════════

**TÌNH HUỐNG 1: Không đủ thông tin trong tài liệu**
Trả lời:
> ⚠️ **Thông tin hạn chế:** Tài liệu hiện có chỉ đề cập đến [những gì có]. Để có thông tin đầy đủ hơn về [vấn đề còn thiếu], bạn nên tham khảo thêm bác sĩ chuyên khoa.

**TÌNH HUỐNG 2: Thông tin mâu thuẫn**
Trả lời:
> 📌 **Lưu ý:** Tài liệu có một số thông tin khác biệt:
> - Quan điểm 1: [...]
> - Quan điểm 2: [...]
> 
> Khuyến nghị: [Tư vấn hợp lý dựa trên ngữ cảnh]

**TÌNH HUỐNG 3: Câu hỏi cần tư vấn y khoa cấp bách**
Luôn thêm:
> 🚨 **QUAN TRỌNG:** Nếu bạn đang gặp các triệu chứng [liệt kê], hãy đến cơ sở y tế NGAY LẬP TỨC hoặc gọi cấp cứu 115.

═══════════════════════════════════════════════════════════════════════════════
📝 CHECKLIST TRƯỚC KHI TRẢ LỜI (TỰ KIỂM TRA)
═══════════════════════════════════════════════════════════════════════════════

Hãy tự hỏi:
☐ Tôi đã đọc KỸ tất cả tài liệu tham khảo chưa?
☐ Tôi đã trích xuất HẾT thông tin liên quan chưa?
☐ Câu trả lời có >= 150 từ không? (trừ câu hỏi đơn giản)
☐ Tôi có sử dụng đầy đủ số liệu, chỉ số từ tài liệu không?
☐ Cấu trúc Markdown có rõ ràng, dễ đọc không?
☐ Tôi có giải thích thuật ngữ phức tạp không?
☐ Có thông tin nào trong tài liệu bị bỏ sót không?

═══════════════════════════════════════════════════════════════════════════════
📥 INPUT
═══════════════════════════════════════════════════════════════════════════════

**CÂU HỎI CỦA BỆNH NHÂN:**
{question}

**TÀI LIỆU THAM KHẢO Y KHOA:**
{context}

═══════════════════════════════════════════════════════════════════════════════
📤 OUTPUT - TRẢ LỜI CHI TIẾT (MARKDOWN FORMAT)
═══════════════════════════════════════════════════════════════════════════════

[BẮT ĐẦU TRẢ LỜI CHI TIẾT TẠI ĐÂY - TUÂN THỦ TẤT CẢ QUY TẮC TRÊN]

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
Bạn là **Bác sĩ chuyên khoa Thận - Tiết niệu** với kiến thức y khoa chuyên sâu. 

📋 **TÌNH HUỐNG:** Câu hỏi sau KHÔNG tìm thấy thông tin cụ thể trong tài liệu chuyên môn hiện có.

🎯 **NHIỆM VỤ:** Trả lời dựa trên kiến thức y khoa TỔNG QUÁT về bệnh thận, tuân thủ các nguyên tắc:

═══════════════════════════════════════════════════════════════════════════════
📐 QUY TẮC TRẢ LỜI
═══════════════════════════════════════════════════════════════════════════════

✅ **BẮT BUỘC:**
1. **BẮT ĐẦU** bằng disclaimer: "**⚠️ Dựa trên kiến thức y khoa chung (không có trong tài liệu chuyên môn):**"
2. **TRẢ LỜI CHI TIẾT** (100-150 từ) - KHÔNG trả lời quá ngắn
3. **CẤU TRÚC RÕ RÀNG** với Markdown formatting đầy đủ
4. **THÔNG TIN CƠ BẢN** và đáng tin cậy nhất
5. **KHUYẾN NGHỊ** tham khảo bác sĩ chuyên khoa nếu cần

❌ **TRÁNH:**
- Trả lời sơ sài < 80 từ
- Thông tin mơ hồ, không chắc chắn
- Tư vấn điều trị cụ thể (thuốc, liều lượng)

═══════════════════════════════════════════════════════════════════════════════
🎨 ĐỊNH DẠNG MARKDOWN BẮT BUỘC
═══════════════════════════════════════════════════════════════════════════════

Sử dụng:
- ## cho tiêu đề chính
- ### cho tiêu đề phụ  
- **bold** cho điểm quan trọng
- *italic* cho nhấn mạnh
- `code` cho thuật ngữ y khoa
- Bullet points (-) cho danh sách
- > cho cảnh báo/lưu ý quan trọng

═══════════════════════════════════════════════════════════════════════════════
❓ CÂU HỎI
═══════════════════════════════════════════════════════════════════════════════

{question}

═══════════════════════════════════════════════════════════════════════════════
💬 TRẢ LỜI CHI TIẾT (MARKDOWN FORMAT)
═══════════════════════════════════════════════════════════════════════════════

"""
            
            response = self.model.generate_content(general_prompt)
            return response.text
            
        except Exception as e:
            return f"""
> 🚨 **LỖI HỆ THỐNG:** Không thể trả lời câu hỏi này do lỗi kỹ thuật.
> 
> **Khuyến nghị:** Vui lòng tham khảo trực tiếp bác sĩ chuyên khoa Thận - Tiết niệu để được tư vấn chính xác.
> 
> **Hotline cấp cứu:** 115 (nếu có triệu chứng nghiêm trọng)
"""

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
                pass
            else:
                # Đọc PDF files
                documents = self.load_pdfs()
                if not documents:
                    return False
                
                # Tạo chunks
                chunks = self.create_chunks(documents)
                if not chunks:
                    return False
                
                # Tạo vector store
                self.vector_store = self.create_vector_store(chunks)
                
                # Lưu vector store
                self.save_vector_store()
            
            # Setup retriever
            self.setup_retriever()
            
            # Tạo QA chain
            self.qa_chain = self.create_qa_chain()
            
            return True
            
        except Exception as e:
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
        while True:
            try:
                question = input("\n❓ Câu hỏi của bạn: ").strip()
                
                if question.lower() in ['quit', 'exit', 'q']:
                    break
                
                if question.lower() == 'rebuild':
                    if self.initialize_system(force_rebuild=True):
                        pass
                    else:
                        pass
                    continue
                
                if not question:
                    continue
                
                print("\n🔍 Đang tìm kiếm thông tin liên quan...")
                result = self.ask_question(question)
                
                print(f"\n💬 **Trả lời (Markdown format):**")
                print(result['response'])
                
            except KeyboardInterrupt:
                break
            except Exception as e:
                pass


def main():
    """Hàm main để chạy chatbot"""
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
            return
        
        # Bắt đầu chat session
        chatbot.start_chat_session()
        
    except Exception as e:
        pass


if __name__ == "__main__":
    main()
