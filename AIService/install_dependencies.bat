@echo off
echo Installing AI Service dependencies...
echo.

REM Activate virtual environment if it exists
if exist venv\Scripts\activate.bat (
    echo Activating virtual environment...
    call venv\Scripts\activate.bat
)

REM Install requirements
echo Installing Python dependencies...
pip install -r requirements.txt

echo.
echo Dependencies installed successfully!
echo.
echo To start the AI Service with Eureka registration:
echo 1. Make sure Eureka Server is running on http://localhost:8761
echo 2. Copy .env.example to .env and configure your settings
echo 3. Run: python main.py
echo.
pause
