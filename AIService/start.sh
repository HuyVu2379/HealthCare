#!/bin/bash

# Start script for AI Service
echo "Starting AI Service..."

# Check if virtual environment exists, create if not
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python -m venv venv
fi

# Activate virtual environment
source venv/bin/activate

# Install dependencies
echo "Installing dependencies..."
pip install -r requirements.txt

# Start the application
echo "Starting FastAPI application..."
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
