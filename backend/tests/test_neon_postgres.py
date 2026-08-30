import asyncio
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from backend.postgres_service import PostgresService
from backend.model_service import ModelService
from backend.feature_engineering import calculate_cognitive_features

def test_neon_postgres_end_to_end_prediction():
    asyncio.run(_run_test_neon_postgres())

async def _run_test_neon_postgres():
    pg_svc = PostgresService()
    assert pg_svc.is_configured(), "DATABASE_URL should be configured in .env"

    test_patient_id = "test_neon_patient_99"

    # 1. Upsert patient
    upsert_res = await pg_svc.upsert_patient(
        patient_id=test_patient_id,
        full_name="Eleanor Vance (Neon Test)",
        age=74,
        date_of_birth="1952-04-15",
        gender="Female",
        email="eleanor.vance@example.com",
        phone="555019283",
        is_completed=True
    )
    assert upsert_res >= 0

    # 2. Insert completed activities across different domains
    await pg_svc.insert_activity(
        activity_id="act_neon_1",
        patient_id=test_patient_id,
        activity_name="Who's Who Family Match",
        score=0.95,
        category="Recognition"
    )
    await pg_svc.insert_activity(
        activity_id="act_neon_2",
        patient_id=test_patient_id,
        activity_name="Face Recall Challenge",
        score=0.88,
        category="Memory"
    )
    await pg_svc.insert_activity(
        activity_id="act_neon_3",
        patient_id=test_patient_id,
        activity_name="Daily Schedule Pattern",
        score=0.90,
        category="Pattern"
    )

    # 3. Retrieve formatted data for Model 1
    raw_input = await pg_svc.get_patient_data_for_prediction(test_patient_id)
    assert raw_input["age"] == 74
    assert raw_input["sessions_completed"] == 3
    assert raw_input["last_game"] in ["memory", "attention", "recognition", "routine", "pattern"]
    assert len(raw_input["memory_scores"]) == 6
    assert len(raw_input["recognition_scores"]) == 6

    # 4. Perform Model 1 prediction
    model_svc = ModelService.get_instance()
    pred_domain, pred_code = model_svc.predict(raw_input)
    assert isinstance(pred_domain, str)
    assert pred_domain in ["memory", "attention", "recognition", "routine", "pattern"]
    assert isinstance(pred_code, int)

    # 5. Verify feature engineering
    eng = calculate_cognitive_features(raw_input)
    assert "memory_history_count" in eng
    assert "memory_recent_avg" in eng
    assert "memory_trend" in eng
    assert "recognition_history_count" in eng
    assert "pattern_history_count" in eng
    print(f"\n[NEON TEST PASSED] Patient {test_patient_id} -> Next Game: {pred_domain} (code: {pred_code})")
