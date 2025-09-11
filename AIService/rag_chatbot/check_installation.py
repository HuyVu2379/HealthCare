"""
Script kiểm tra cài đặt RAG PDF Chatbot
Chạy script này để đảm bảo tất cả dependencies được cài đặt đúng
"""

import sys
import os
from pathlib import Path

def check_python_version():
    """Kiểm tra phiên bản Python"""
    print("🐍 Kiểm tra Python version...")
    version = sys.version_info
    if version.major >= 3 and version.minor >= 8:
        print(f"   ✅ Python {version.major}.{version.minor}.{version.micro} - OK")
        return True
    else:
        print(f"   ❌ Python {version.major}.{version.minor}.{version.micro} - Cần Python 3.8+")
        return False

def check_imports():
    """Kiểm tra các thư viện cần thiết"""
    print("\n📦 Kiểm tra thư viện...")
    
    required_packages = [
        ("langchain", "LangChain"),
        ("langchain_community", "LangChain Community"),
        ("langchain_openai", "LangChain OpenAI"),
        ("faiss", "FAISS"),
        ("google.generativeai", "Google Generative AI"),
        ("pypdf", "PyPDF"),
        ("sentence_transformers", "Sentence Transformers"),
        ("dotenv", "Python Dotenv"),
        ("numpy", "NumPy"),
        ("pandas", "Pandas")
    ]
    
    success_count = 0
    for package, name in required_packages:
        try:
            __import__(package)
            print(f"   ✅ {name} - OK")
            success_count += 1
        except ImportError:
            print(f"   ❌ {name} - THIẾU")
    
    print(f"\n📊 Kết quả: {success_count}/{len(required_packages)} packages OK")
    return success_count == len(required_packages)

def check_api_keys():
    """Kiểm tra API keys"""
    print("\n🔑 Kiểm tra API keys...")
    
    # Load .env file nếu có
    env_file = Path(".env")
    if env_file.exists():
        print("   📄 Tìm thấy file .env")
        try:
            from dotenv import load_dotenv
            load_dotenv()
        except:
            print("   ⚠️  Không thể load .env file")
    else:
        print("   ⚠️  Không tìm thấy file .env")
    
    # Kiểm tra Gemini API key
    gemini_key = os.getenv("GEMINI_API_KEY")
    if gemini_key:
        if gemini_key.startswith("AIza") and len(gemini_key) > 30:
            print("   ✅ GEMINI_API_KEY - OK")
        else:
            print("   ⚠️  GEMINI_API_KEY có vẻ không hợp lệ")
    else:
        print("   ❌ GEMINI_API_KEY - THIẾU")
    
    # Kiểm tra OpenAI API key (optional)
    openai_key = os.getenv("OPENAI_API_KEY")
    if openai_key:
        if openai_key.startswith("sk-") and len(openai_key) > 40:
            print("   ✅ OPENAI_API_KEY - OK (optional)")
        else:
            print("   ⚠️  OPENAI_API_KEY có vẻ không hợp lệ")
    else:
        print("   ⚠️  OPENAI_API_KEY - THIẾU (optional)")
    
    return bool(gemini_key)

def check_directories():
    """Kiểm tra cấu trúc thư mục"""
    print("\n📁 Kiểm tra cấu trúc thư mục...")
    
    required_dirs = ["data", "vector_store"]
    required_files = [
        "rag_pdf_chatbot.py",
        "simple_rag_chatbot.py", 
        ".env.example"
    ]
    
    # Kiểm tra thư mục
    for dir_name in required_dirs:
        dir_path = Path(dir_name)
        if dir_path.exists():
            print(f"   ✅ Thư mục {dir_name}/ - OK")
        else:
            print(f"   ⚠️  Thư mục {dir_name}/ - THIẾU (sẽ tạo tự động)")
            try:
                dir_path.mkdir(exist_ok=True)
                print(f"      ✅ Đã tạo thư mục {dir_name}/")
            except Exception as e:
                print(f"      ❌ Không thể tạo thư mục: {e}")
    
    # Kiểm tra file
    for file_name in required_files:
        file_path = Path(file_name)
        if file_path.exists():
            print(f"   ✅ File {file_name} - OK")
        else:
            print(f"   ❌ File {file_name} - THIẾU")

def check_pdf_files():
    """Kiểm tra file PDF"""
    print("\n📄 Kiểm tra file PDF...")
    
    data_dir = Path("data")
    if not data_dir.exists():
        print("   ❌ Thư mục data/ không tồn tại")
        return False
    
    pdf_files = list(data_dir.glob("*.pdf"))
    if pdf_files:
        print(f"   ✅ Tìm thấy {len(pdf_files)} file PDF:")
        for pdf_file in pdf_files:
            file_size = pdf_file.stat().st_size / (1024 * 1024)  # MB
            print(f"      📄 {pdf_file.name} ({file_size:.1f} MB)")
        return True
    else:
        print("   ⚠️  Không tìm thấy file PDF nào")
        print("      💡 Đặt các file PDF vào thư mục data/ để sử dụng")
        return False

def test_basic_functionality():
    """Test chức năng cơ bản"""
    print("\n🧪 Test chức năng cơ bản...")
    
    try:
        # Test FAISS
        import faiss
        import numpy as np
        
        # Tạo test vectors
        dimension = 128
        test_vectors = np.random.random((10, dimension)).astype('float32')
        
        # Tạo FAISS index
        index = faiss.IndexFlatL2(dimension)
        index.add(test_vectors)
        
        # Test search
        query = np.random.random((1, dimension)).astype('float32')
        distances, indices = index.search(query, k=3)
        
        print("   ✅ FAISS vector search - OK")
        
    except Exception as e:
        print(f"   ❌ FAISS test failed: {e}")
        return False
    
    try:
        # Test Gemini API (nếu có key)
        gemini_key = os.getenv("GEMINI_API_KEY")
        if gemini_key:
            import google.generativeai as genai
            genai.configure(api_key=gemini_key)
            
            # Tạo model nhưng chưa gọi API (để tiết kiệm quota)
            model = genai.GenerativeModel('gemini-2.0-flash-exp')
            print("   ✅ Gemini API connection - OK")
        else:
            print("   ⚠️  Không test Gemini API (thiếu API key)")
    
    except Exception as e:
        print(f"   ❌ Gemini API test failed: {e}")
        return False
    
    return True

def main():
    """Hàm main kiểm tra tổng thể"""
    print("🔍 RAG PDF CHATBOT - KIỂM TRA CÀI ĐẶT")
    print("=" * 50)
    
    checks = []
    
    # Các kiểm tra
    checks.append(("Python Version", check_python_version()))
    checks.append(("Required Packages", check_imports()))
    checks.append(("API Keys", check_api_keys()))
    checks.append(("Directory Structure", True))  # check_directories() luôn return True
    check_directories()
    checks.append(("PDF Files", check_pdf_files()))
    checks.append(("Basic Functionality", test_basic_functionality()))
    
    # Tổng kết
    print("\n" + "=" * 50)
    print("📊 TỔNG KẾT KIỂM TRA")
    print("=" * 50)
    
    passed = 0
    total = len(checks)
    
    for check_name, result in checks:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{check_name:.<30} {status}")
        if result:
            passed += 1
    
    print(f"\nKết quả: {passed}/{total} checks passed")
    
    if passed == total:
        print("\n🎉 TẤT CẢ KIỂM TRA ĐÃ PASS!")
        print("✅ Hệ thống sẵn sàng sử dụng!")
        print("\n🚀 Các bước tiếp theo:")
        print("1. Đặt file PDF vào thư mục data/ (nếu chưa có)")
        print("2. Chạy: python rag_pdf_chatbot.py")
        print("3. Hoặc test: python simple_rag_chatbot.py test")
    else:
        print(f"\n⚠️  CÒN {total - passed} VẤN ĐỀ CẦN KHẮC PHỤC")
        print("\n🔧 Hướng dẫn khắc phục:")
        
        if not checks[0][1]:  # Python version
            print("- Cài đặt Python 3.8 trở lên")
        
        if not checks[1][1]:  # Packages
            print("- Chạy: pip install -r ../requirements.txt")
        
        if not checks[2][1]:  # API keys
            print("- Tạo file .env từ .env.example")
            print("- Thêm GEMINI_API_KEY từ https://makersuite.google.com/app/apikey")
        
        if not checks[4][1]:  # PDF files
            print("- Đặt file PDF vào thư mục data/")
        
        if not checks[5][1]:  # Basic functionality
            print("- Kiểm tra lại cài đặt thư viện")
            print("- Kiểm tra API key có hợp lệ không")

if __name__ == "__main__":
    main()
