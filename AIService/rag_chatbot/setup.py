"""
Script setup và cài đặt cho RAG PDF Chatbot
"""

import os
import sys
from pathlib import Path
import subprocess

def install_requirements():
    """Cài đặt các thư viện cần thiết"""
    print("📦 Đang cài đặt thư viện cần thiết...")
    
    requirements = [
        "pypdf==4.3.1",
        "faiss-cpu==1.8.0", 
        "google-generativeai==0.8.0",
        "sentence-transformers==3.0.1",
        "langchain==0.3.0",
        "langchain-community==0.3.0",
        "langchain-text-splitters==0.3.0",
        "python-dotenv==1.0.1"
    ]
    
    for req in requirements:
        try:
            print(f"   Installing {req}...")
            subprocess.check_call([sys.executable, "-m", "pip", "install", req])
            print(f"   ✅ {req} installed successfully")
        except subprocess.CalledProcessError as e:
            print(f"   ❌ Failed to install {req}: {e}")

def create_directories():
    """Tạo các thư mục cần thiết"""
    print("📁 Tạo cấu trúc thư mục...")
    
    current_dir = Path(__file__).parent
    
    # Tạo thư mục data
    data_dir = current_dir / "data"
    data_dir.mkdir(exist_ok=True)
    print(f"   ✅ Created: {data_dir}")
    
    # Tạo thư mục vector_store
    vector_store_dir = current_dir / "vector_store" 
    vector_store_dir.mkdir(exist_ok=True)
    print(f"   ✅ Created: {vector_store_dir}")

def setup_env_file():
    """Thiết lập file .env"""
    print("⚙️  Thiết lập file environment...")
    
    current_dir = Path(__file__).parent
    env_file = current_dir / ".env"
    env_example_file = current_dir / ".env.example"
    
    if not env_file.exists() and env_example_file.exists():
        # Copy .env.example to .env
        with open(env_example_file, 'r') as f:
            content = f.read()
        
        with open(env_file, 'w') as f:
            f.write(content)
        
        print(f"   ✅ Created .env from .env.example")
        print(f"   ⚠️  Please edit .env and add your GEMINI_API_KEY")
    else:
        print(f"   ℹ️  .env file already exists")

def check_pdf_files():
    """Kiểm tra file PDF"""
    print("📄 Kiểm tra file PDF...")
    
    current_dir = Path(__file__).parent
    data_dir = current_dir / "data"
    
    pdf_files = list(data_dir.glob("*.pdf"))
    
    if pdf_files:
        print(f"   ✅ Found {len(pdf_files)} PDF files:")
        for pdf in pdf_files:
            print(f"      - {pdf.name}")
    else:
        print(f"   ⚠️  No PDF files found in {data_dir}")
        print(f"   Please add your PDF files to the data/ directory")

def main():
    """Main setup function"""
    print("🚀 RAG PDF Chatbot Setup")
    print("=" * 40)
    
    try:
        # install_requirements()  # Uncomment if needed
        create_directories()
        setup_env_file()
        check_pdf_files()
        
        print("\n" + "=" * 40)
        print("✅ Setup completed successfully!")
        print("\n📋 Next steps:")
        print("1. Add your PDF files to the data/ directory")
        print("2. Edit .env file and add your GEMINI_API_KEY")
        print("3. Run: python rag_pdf_chatbot.py")
        
    except Exception as e:
        print(f"❌ Setup failed: {e}")

if __name__ == "__main__":
    main()
