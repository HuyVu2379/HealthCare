#!/bin/bash

echo "========================================"
echo "RAG PDF Chatbot - Setup Script"
echo "========================================"

echo ""
echo "1. Installing Python packages..."
echo "========================================"
pip install --upgrade pip
pip install -r ../requirements.txt

echo ""
echo "2. Checking installation..."
echo "========================================"
python -c "import langchain; print('✅ LangChain installed')"
python -c "import faiss; print('✅ FAISS installed')"
python -c "import google.generativeai; print('✅ Google Generative AI installed')"
python -c "import pypdf; print('✅ PyPDF installed')"

echo ""
echo "3. Creating directories..."
echo "========================================"
mkdir -p data
mkdir -p vector_store
echo "✅ Directories created"

echo ""
echo "4. Setup completed!"
echo "========================================"
echo "Next steps:"
echo "1. Copy your PDF files to data/ folder"
echo "2. Get Gemini API key from: https://makersuite.google.com/app/apikey"
echo "3. Copy .env.example to .env and add your API key"
echo "4. Run: python rag_pdf_chatbot.py"
echo ""
