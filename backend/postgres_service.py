"""PostgreSQL Service for Cogniva Backend connected to Neon Database

Provides queries for patient profile retrieval, game session records,
and data preparation for Model 1 XGBoost next-game recommendations.
"""

import logging
from typing import Dict, Any, List, Optional
from backend.database import execute_query, execute_statement, is_db_configured

logger = logging.getLogger("cogniva_postgres")

class PostgresService:
    """Service to interact with Neon PostgreSQL database for patient metrics & prediction."""

    @staticmethod
    def is_configured() -> bool:
        return is_db_configured()

    async def get_patient_data_for_prediction(
        self,
        patient_id: str,
        user_token: Optional[str] = None
    ) -> Dict[str, Any]:
        """Fetch patient age, assessment baselines, and completed activity history
        from Neon PostgreSQL, structured precisely for Model 1 feature engineering.
        """
        logger.info(f"Querying Neon PostgreSQL for patient_id: {patient_id}")

        # 1. Fetch patient profile
        patient_rows = await execute_query(
            "SELECT id, full_name, age, date_of_birth, gender FROM patients WHERE id = %s LIMIT 1",
            (patient_id,)
        )
        patient_age = 70  # Default fallback if not found
        if patient_rows and patient_rows[0].get("age") is not None:
            patient_age = int(patient_rows[0]["age"])

        # 2. Fetch initial assessment for baseline domain scores
        assessment_rows = await execute_query(
            """SELECT memory_score, attention_score, overall_score 
               FROM assessments 
               WHERE patient_id = %s 
               ORDER BY completed_at ASC 
               LIMIT 1""",
            (patient_id,)
        )
        
        initial_memory = 50.0
        initial_attention = 50.0
        initial_recognition = 50.0
        initial_routine = 50.0
        initial_pattern = 50.0

        if assessment_rows:
            assess = assessment_rows[0]
            mem = assess.get("memory_score")
            att = assess.get("attention_score")
            overall = assess.get("overall_score")

            if mem is not None:
                initial_memory = float(mem) * 100.0 if float(mem) <= 1.0 else float(mem)
            if att is not None:
                initial_attention = float(att) * 100.0 if float(att) <= 1.0 else float(att)
            if overall is not None:
                val = float(overall) * 100.0 if float(overall) <= 1.0 else float(overall)
                initial_recognition = val
                initial_routine = val
                initial_pattern = val

        # 3. Fetch completed game activities
        activities = await execute_query(
            """SELECT id, activity_name, score, category, completed_at 
               FROM activities 
               WHERE patient_id = %s 
               ORDER BY completed_at ASC""",
            (patient_id,)
        )

        sessions_completed = len(activities)
        last_game = "none"

        domain_scores: Dict[str, List[float]] = {
            "memory": [],
            "attention": [],
            "recognition": [],
            "routine": [],
            "pattern": []
        }

        for act in activities:
            name = (act.get("activity_name") or "").lower()
            cat = (act.get("category") or "").lower()
            raw_score = act.get("score", 0.0)

            # Convert 0.0-1.0 fraction to 0-100 scale if needed
            score_val = float(raw_score) * 100.0 if float(raw_score) <= 1.0 else float(raw_score)

            # Map game to cognitive domain
            domain = "memory"
            if "who" in name or "face" in name or "family" in name or "recognition" in cat:
                domain = "recognition"
            elif "pattern" in name or "sequence" in name or "pattern" in cat:
                domain = "pattern"
            elif "routine" in name or "schedule" in name or "routine" in cat:
                domain = "routine"
            elif "attention" in name or "focus" in name or "attention" in cat:
                domain = "attention"
            else:
                domain = "memory"

            domain_scores[domain].append(score_val)
            last_game = domain

        # Construct 6-slot recent history arrays (-1.0 for unpopulated slots)
        def get_recent_6_slots(scores: List[float]) -> List[float]:
            slots = [-1.0] * 6
            recent = scores[-6:] if len(scores) >= 6 else scores
            for i, s in enumerate(recent):
                slots[i] = round(float(s), 2)
            return slots

        raw_input = {
            "patient_id": patient_id,
            "age": patient_age,
            "initial_memory": initial_memory,
            "initial_attention": initial_attention,
            "initial_recognition": initial_recognition,
            "initial_routine": initial_routine,
            "initial_pattern": initial_pattern,
            "last_game": last_game,
            "sessions_completed": sessions_completed,
            "memory_scores": get_recent_6_slots(domain_scores["memory"]),
            "attention_scores": get_recent_6_slots(domain_scores["attention"]),
            "recognition_scores": get_recent_6_slots(domain_scores["recognition"]),
            "routine_scores": get_recent_6_slots(domain_scores["routine"]),
            "pattern_scores": get_recent_6_slots(domain_scores["pattern"]),
        }

        return raw_input

    async def upsert_patient(
        self,
        patient_id: str,
        full_name: str,
        age: Optional[int] = None,
        date_of_birth: Optional[str] = None,
        gender: Optional[str] = None,
        email: Optional[str] = None,
        phone: Optional[str] = None,
        is_completed: bool = True
    ) -> int:
        """Insert or update a patient in Neon PostgreSQL."""
        stmt = """
        INSERT INTO patients (id, full_name, age, date_of_birth, gender, email, phone, is_completed, updated_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, CURRENT_TIMESTAMP)
        ON CONFLICT (id) DO UPDATE SET
            full_name = EXCLUDED.full_name,
            age = COALESCE(EXCLUDED.age, patients.age),
            date_of_birth = COALESCE(EXCLUDED.date_of_birth, patients.date_of_birth),
            gender = COALESCE(EXCLUDED.gender, patients.gender),
            email = COALESCE(EXCLUDED.email, patients.email),
            phone = COALESCE(EXCLUDED.phone, patients.phone),
            is_completed = EXCLUDED.is_completed,
            updated_at = CURRENT_TIMESTAMP;
        """
        return await execute_statement(
            stmt,
            (patient_id, full_name, age, date_of_birth, gender, email, phone, is_completed)
        )

    async def insert_activity(
        self,
        activity_id: str,
        patient_id: str,
        activity_name: str,
        score: float,
        category: str = "Memory"
    ) -> int:
        """Record a completed game activity in Neon PostgreSQL."""
        stmt = """
        INSERT INTO activities (id, patient_id, activity_name, score, category, completed_at)
        VALUES (%s, %s, %s, %s, %s, CURRENT_TIMESTAMP)
        ON CONFLICT (id) DO NOTHING;
        """
        return await execute_statement(stmt, (activity_id, patient_id, activity_name, score, category))

    async def upsert_caregiver(
        self,
        caregiver_id: str,
        full_name: str,
        email: Optional[str] = None,
        phone: Optional[str] = None,
        patient_relationship: Optional[str] = None,
        linked_patient_id: Optional[str] = None,
        is_completed: bool = True
    ) -> int:
        """Insert or update a caregiver in Neon PostgreSQL."""
        stmt = """
        INSERT INTO caregivers (id, full_name, email, phone, patient_relationship, linked_patient_id, is_completed, updated_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, CURRENT_TIMESTAMP)
        ON CONFLICT (id) DO UPDATE SET
            full_name = EXCLUDED.full_name,
            email = COALESCE(EXCLUDED.email, caregivers.email),
            phone = COALESCE(EXCLUDED.phone, caregivers.phone),
            patient_relationship = COALESCE(EXCLUDED.patient_relationship, caregivers.patient_relationship),
            linked_patient_id = COALESCE(EXCLUDED.linked_patient_id, caregivers.linked_patient_id),
            is_completed = EXCLUDED.is_completed,
            updated_at = CURRENT_TIMESTAMP;
        """
        return await execute_statement(
            stmt,
            (caregiver_id, full_name, email, phone, patient_relationship, linked_patient_id, is_completed)
        )

