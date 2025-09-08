@echo off
REM Start script for AI Service on Windows

echo Starting AI Service...

REM Check if virtual environment exists, create if not
if not exist "venv" (
    echo Creating virtual environment...
    python -m venv venv
)

REM Activate virtual environment
call venv\Scripts\activate

REM Install dependencies
echo Installing dependencies...
pip install -r requirements.txt

REM Start the application
echo Starting FastAPI application...
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
