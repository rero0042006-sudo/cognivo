"""Cloud Firestore Data Retrieval Service for Cogniva Backend

Retrieves patient demographic data, initial assessment scores, and chronologically
sorted completed activities from Cloud Firestore to feed the Model 1 feature
engineering pipeline.
"""

import os
import logging
from typing import Any, Dict, List, Optional
import httpx
from backend.domain_mapping import DOMAINS, map_game_to_domain

logger = logging.getLogger("firebase_service")

class FirebaseService:
    def __init__(self):
        self.project_id = (
            os.getenv("FIREBASE_PROJECT_ID")
            or os.getenv("VITE_FIREBASE_PROJECT_ID")
            or "cogniva-772ce"
        )
        self.api_key = (
            os.getenv("FIREBASE_API_KEY")
            or os.getenv("VITE_FIREBASE_API_KEY")
            or "AIzaSyAsEJVWO-sPvO1nclSbnHlvAD_o9gc2si4"
        )
        self.firestore_base_url = (
            f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents"
        )

    def _extract_val(self, field_dict: Optional[Dict[str, Any]]) -> Any:
        if not field_dict or not isinstance(field_dict, dict):
            return None
        if "stringValue" in field_dict:
            return field_dict["stringValue"]
        if "integerValue" in field_dict:
            return int(field_dict["integerValue"])
        if "doubleValue" in field_dict:
            return float(field_dict["doubleValue"])
        if "booleanValue" in field_dict:
            return field_dict["booleanValue"]
        if "timestampValue" in field_dict:
            return field_dict["timestampValue"]
        return None

    async def get_patient_data_for_prediction(
        self,
        patient_id: str,
        user_token: Optional[str] = None
    ) -> Dict[str, Any]:
        """Fetch all required patient data from Firestore and format into raw model input."""
        headers = {"Content-Type": "application/json"}
        if user_token:
            headers["Authorization"] = f"Bearer {user_token}"

        async with httpx.AsyncClient(timeout=10.0) as client:
            # 1. Fetch patient document: users/{patient_id}
            age = 70.0
            try:
                p_resp = await client.get(
                    f"{self.firestore_base_url}/users/{patient_id}?key={self.api_key}",
                    headers=headers
                )
                if p_resp.status_code == 200:
                    fields = p_resp.json().get("fields", {})
                    age_val = self._extract_val(fields.get("age"))
                    if age_val is not None:
                        age = float(age_val)
            except Exception as e:
                logger.warning(f"Could not fetch patient age for {patient_id} from Firestore: {e}")

            # 2. Fetch initial assessment: users/{patient_id}/assessments
            initial_memory = 50.0
            initial_attention = 50.0
            initial_recognition = 50.0
            initial_routine = 50.0
            initial_pattern = 50.0

            try:
                a_resp = await client.get(
                    f"{self.firestore_base_url}/users/{patient_id}/assessments?key={self.api_key}",
                    headers=headers
                )
                if a_resp.status_code == 200:
                    documents = a_resp.json().get("documents", [])
                    if documents:
                        # Sort by createTime ascending
                        documents.sort(key=lambda d: d.get("createTime", ""))
                        first_doc = documents[0]
                        fields = first_doc.get("fields", {})

                        def norm_score(f_name: str, default: float) -> float:
                            val = self._extract_val(fields.get(f_name))
                            if val is None:
                                return default
                            v = float(val)
                            return v * 100.0 if 0.0 <= v <= 1.0 else v

                        initial_memory = norm_score("memoryScore", 50.0)
                        initial_attention = norm_score("attentionScore", 50.0)
                        initial_recognition = norm_score("recognitionScore", 50.0)
                        initial_routine = norm_score("routineScore", 50.0)
                        initial_pattern = norm_score("patternScore", 50.0)
            except Exception as e:
                logger.warning(f"Could not fetch initial assessment for {patient_id} from Firestore: {e}")

            # 3. Fetch completed activities: users/{patient_id}/activities
            domain_scores: Dict[str, List[float]] = {d: [] for d in DOMAINS}
            last_game = "none"
            sessions_completed = 0

            try:
                act_resp = await client.get(
                    f"{self.firestore_base_url}/users/{patient_id}/activities?key={self.api_key}",
                    headers=headers
                )
                if act_resp.status_code == 200:
                    documents = act_resp.json().get("documents", [])
                    sessions_completed = len(documents)
                    if documents:
                        # Sort by completedAt or createTime ascending
                        documents.sort(key=lambda d: self._extract_val(d.get("fields", {}).get("completedAt")) or d.get("createTime", ""))
                        last_doc = documents[-1]
                        last_act_name = self._extract_val(last_doc.get("fields", {}).get("activityName")) or ""
                        last_game = map_game_to_domain(last_act_name)

                        for doc in documents:
                            fields = doc.get("fields", {})
                            act_name = self._extract_val(fields.get("activityName")) or ""
                            domain = map_game_to_domain(act_name)
                            score_val = self._extract_val(fields.get("score"))
                            if score_val is not None and domain in domain_scores:
                                val = float(score_val)
                                score_pct = val * 100.0 if 0.0 <= val <= 1.0 else val
                                domain_scores[domain].append(score_pct)
            except Exception as e:
                logger.warning(f"Could not fetch activities for {patient_id} from Firestore: {e}")

            return {
                "patient_id": patient_id,
                "age": age,
                "initial_memory": initial_memory,
                "initial_attention": initial_attention,
                "initial_recognition": initial_recognition,
                "initial_routine": initial_routine,
                "initial_pattern": initial_pattern,
                "domain_scores": domain_scores,
                "last_game": last_game,
                "sessions_completed": sessions_completed,
            }
