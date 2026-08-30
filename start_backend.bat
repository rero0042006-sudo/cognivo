@echo off
echo Starting Cogniva Model 1 FastAPI Backend Server on port 8000...
py -3 -m uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
pause
