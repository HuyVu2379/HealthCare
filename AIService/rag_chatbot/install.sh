#!/bin/bash

# Setup script for RAG PDF Chatbot on Linux/Mac

echo "🚀 RAG PDF Chatbot Setup (Linux/Mac)"
echo "======================================"

# Check Python version
python_version=$(python3 --version 2>&1)
echo "📍 Python version: $python_version"

# Create virtual environment
echo "🔧 Creating virtual environment..."
python3 -m venv venv
source venv/bin/activate

# Upgrade pip
echo "📦 Upgrading pip..."
pip install --upgrade pip

# Install requirements
echo "📚 Installing requirements..."
pip install pypdf==4.3.1 \
           faiss-cpu==1.8.0 \
           google-generativeai==0.8.0 \
           sentence-transformers==3.0.1 \
           langchain==0.3.0 \
           langchain-community==0.3.0 \
           langchain-text-splitters==0.3.0 \
           python-dotenv==1.0.1

# Create directories
echo "📁 Creating directories..."
mkdir -p data
mkdir -p vector_store

# Setup .env file
echo "⚙️ Setting up .env file..."
if [ ! -f .env ] && [ -f .env.example ]; then
    cp .env.example .env
    echo "✅ Created .env from .env.example"
    echo "⚠️  Please edit .env and add your GEMINI_API_KEY"
fi

# Check for PDF files
echo "📄 Checking for PDF files..."
pdf_count=$(find data -name "*.pdf" 2>/dev/null | wc -l)
if [ $pdf_count -gt 0 ]; then
    echo "✅ Found $pdf_count PDF files in data/ directory"
else
    echo "⚠️  No PDF files found in data/ directory"
    echo "   Please add your PDF files to the data/ directory"
fi

echo ""
echo "======================================"
echo "✅ Setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Add your PDF files to the data/ directory"
echo "2. Edit .env file and add your GEMINI_API_KEY"
echo "3. Activate virtual environment: source venv/bin/activate"
echo "4. Run: python rag_pdf_chatbot.py"
