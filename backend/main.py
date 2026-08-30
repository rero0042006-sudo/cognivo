"""FastAPI Backend Server for Cogniva Cognitive Training Application

Provides Model 1 XGBoost Next-Game Recommendation API connected to Supabase.
"""

import os
import sys
import logging
from contextlib import asynccontextmanager
from typing import Optional

# Ensure project root is in sys.path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fastapi import FastAPI, HTTPException, Header, Path, Query
from fastapi.middleware.cors import CORSMiddleware
from backend.model_service import ModelService
from backend.postgres_service import PostgresService
from backend.firebase_service import FirebaseService
from backend.feature_engineering import calculate_cognitive_features
from backend.schemas import (
    NextGameResponse,
    DirectPredictionRequest,
    HealthResponse,
    PatientCreateRequest,
    ActivityCreateRequest,
    CaregiverCreateRequest
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("cogniva_backend")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Load Model 1 singleton
    logger.info("Initializing Cogniva Backend Services...")
    try:
        model_svc = ModelService.get_instance()
        logger.info(f"Model 1 loaded with classes: {list(model_svc.target_encoder.classes_)}")
    except Exception as e:
        logger.error(f"Startup model load failed: {e}", exc_info=True)
    yield
    # Shutdown
    logger.info("Shutting down Cogniva Backend...")

app = FastAPI(
    title="Cogniva Cognitive-Training Recommendation API",
    description="Backend service for patient cognitive domain recommendation using Model 1 XGBoost, Neon PostgreSQL, and Cloud Firestore.",
    version="1.0.0",
    lifespan=lifespan
)

# Enable CORS for frontend and local Android emulator
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

postgres_service = PostgresService()
firebase_service = FirebaseService()

@app.get("/health", response_model=HealthResponse, tags=["System"])
async def health_check():
    """Health check endpoint providing model loading status, database connectivity, and target classes."""
    try:
        model_svc = ModelService.get_instance()
        classes = list(model_svc.target_encoder.classes_) if model_svc.target_encoder is not None else []
        db_status = "connected" if postgres_service.is_configured() else "unconfigured"
        return HealthResponse(
            status=f"ok (neon_db: {db_status})",
            model_loaded=model_svc.is_loaded,
            target_classes=classes
        )
    except Exception as e:
        return HealthResponse(
            status=f"error: {str(e)}",
            model_loaded=False,
            target_classes=[]
        )

@app.get(
    "/api/patients/{patient_id}/next-game",
    response_model=NextGameResponse,
    tags=["Recommendations"],
    summary="Predict Next Recommended Cognitive Game Domain for a Patient"
)
async def predict_next_game_for_patient(
    patient_id: str = Path(..., description="The patient's unique UUID or identifier"),
    authorization: Optional[str] = Header(None, description="Bearer token for authentication")
):
    """Retrieve patient initial assessment and completed game history from Neon PostgreSQL
    (or Cloud Firestore fallback), perform deterministic feature engineering on the 6-score slots per domain,
    run Model 1 XGBoost inference, and return the predicted next game domain.
    """
    logger.info(f"Received next-game prediction request for patient_id: {patient_id}")
    
    # Extract token
    token = None
    if authorization and authorization.startswith("Bearer "):
        token = authorization.split("Bearer ")[1].strip()

    try:
        raw_input = None

        # 1. Fetch patient data from Neon PostgreSQL (Primary)
        if postgres_service.is_configured():
            try:
                pg_input = await postgres_service.get_patient_data_for_prediction(
                    patient_id=patient_id,
                    user_token=token
                )
                if pg_input is not None:
                    raw_input = pg_input
            except Exception as e:
                logger.warning(f"Error querying Neon PostgreSQL: {e}")

        # 2. Fallback to Cloud Firestore if no data in Neon
        if raw_input is None:
            raw_input = await firebase_service.get_patient_data_for_prediction(
                patient_id=patient_id,
                user_token=token
            )

        # 3. Perform feature engineering & Model 1 inference
        model_svc = ModelService.get_instance()
        pred_domain, pred_code = model_svc.predict(raw_input)
        eng_features = calculate_cognitive_features(raw_input)

        logger.info(
            f"Successfully predicted next_game '{pred_domain}' (code {pred_code}) for patient {patient_id} "
            f"[sessions: {raw_input.get('sessions_completed')}, last_game: {raw_input.get('last_game')}]"
        )

        return NextGameResponse(
            patient_id=patient_id,
            next_game=pred_domain,
            predicted_class_code=pred_code,
            sessions_completed=raw_input.get("sessions_completed", 0),
            last_game=raw_input.get("last_game", "none"),
            engineered_features=eng_features
        )
    except Exception as e:
        logger.error(f"Error predicting next game for patient {patient_id}: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail="Failed to generate next-game recommendation."
        )

@app.post(
    "/api/predict/direct",
    response_model=NextGameResponse,
    tags=["Recommendations"],
    summary="Direct Feature Vector Prediction (Testing / Custom Input)"
)
async def predict_direct(request: DirectPredictionRequest):
    """Directly provide score slots and patient data for Model 1 prediction."""
    raw_input = {
        "age": request.age,
        "initial_memory": request.initial_memory,
        "initial_attention": request.initial_attention,
        "initial_recognition": request.initial_recognition,
        "initial_routine": request.initial_routine,
        "initial_pattern": request.initial_pattern,
        "last_game": request.last_game,
        "sessions_completed": request.sessions_completed,
    }

    # Populate domain score slots
    domain_map = {
        "memory": request.memory_scores,
        "attention": request.attention_scores,
        "recognition": request.recognition_scores,
        "routine": request.routine_scores,
        "pattern": request.pattern_scores,
    }

    for domain, scores in domain_map.items():
        padded = scores[:6] + [-1.0] * (6 - len(scores[:6]))
        for idx, s in enumerate(padded, start=1):
            raw_input[f"{domain}_score_{idx}"] = s

    try:
        model_svc = ModelService.get_instance()
        pred_domain, pred_code = model_svc.predict(raw_input)
        eng_features = calculate_cognitive_features(raw_input)

        return NextGameResponse(
            patient_id="direct_test",
            next_game=pred_domain,
            predicted_class_code=pred_code,
            sessions_completed=request.sessions_completed,
            last_game=request.last_game,
            engineered_features=eng_features
        )
    except Exception as e:
        logger.error(f"Direct prediction failed: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

@app.post("/api/patients", tags=["Patients"], summary="Upsert Patient Profile in Neon PostgreSQL")
async def create_or_update_patient(request: PatientCreateRequest):
    """Save patient details to Neon PostgreSQL."""
    try:
        await postgres_service.upsert_patient(
            patient_id=request.id,
            full_name=request.full_name,
            age=request.age,
            date_of_birth=request.date_of_birth,
            gender=request.gender,
            email=request.email,
            phone=request.phone,
            is_completed=request.is_completed
        )
        return {"status": "ok", "patient_id": request.id, "message": "Patient profile saved to Neon PostgreSQL."}
    except Exception as e:
        logger.error(f"Error saving patient to Neon PostgreSQL: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@app.post("/api/activities", tags=["Activities"], summary="Record Completed Activity in Neon PostgreSQL")
async def record_activity(request: ActivityCreateRequest):
    """Save a game activity record to Neon PostgreSQL."""
    try:
        await postgres_service.insert_activity(
            activity_id=request.id,
            patient_id=request.patient_id,
            activity_name=request.activity_name,
            score=request.score,
            category=request.category
        )
        return {"status": "ok", "activity_id": request.id, "message": "Activity recorded in Neon PostgreSQL."}
    except Exception as e:
        logger.error(f"Error recording activity in Neon PostgreSQL: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@app.post("/api/caregivers", tags=["Caregivers"], summary="Upsert Caregiver Profile in Neon PostgreSQL")
async def create_or_update_caregiver(request: CaregiverCreateRequest):
    """Save caregiver details to Neon PostgreSQL."""
    try:
        await postgres_service.upsert_caregiver(
            caregiver_id=request.id,
            full_name=request.full_name,
            email=request.email,
            phone=request.phone,
            patient_relationship=request.patient_relationship,
            linked_patient_id=request.linked_patient_id,
            is_completed=request.is_completed
        )
        return {"status": "ok", "caregiver_id": request.id, "message": "Caregiver profile saved to Neon PostgreSQL."}
    except Exception as e:
        logger.error(f"Error saving caregiver to Neon PostgreSQL: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.main:app", host="0.0.0.0", port=8000, reload=True)
