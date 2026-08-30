"""Model Service for Cogniva Model 1 XGBoost Next-Game Recommendation

Loads cogniva_model1_xgboost.pkl singleton, builds the exact 53-feature DataFrame,
validates feature schema, and predicts next_game domain.
"""

import os
import sys
import logging
from typing import Any, Dict, List, Tuple
import pandas as pd
import numpy as np

logger = logging.getLogger("model_service")

# Scikit-learn 1.6 vs 1.7 compatibility shim for _RemainderColsList
try:
    import sklearn.compose._column_transformer
    class _RemainderColsList(list):
        pass
    if not hasattr(sklearn.compose._column_transformer, "_RemainderColsList"):
        setattr(sklearn.compose._column_transformer, "_RemainderColsList", _RemainderColsList)
except Exception as ex:
    logger.warning(f"Could not inject _RemainderColsList shim: {ex}")

import joblib
from backend.feature_engineering import DOMAINS, calculate_cognitive_features

# Exact 53 feature columns expected by Model 1 in exact training order
EXPECTED_FEATURES = [
    "age",
    "initial_memory",
    "initial_attention",
    "initial_recognition",
    "initial_routine",
    "initial_pattern",
    "memory_score_1", "memory_score_2", "memory_score_3", "memory_score_4", "memory_score_5", "memory_score_6",
    "attention_score_1", "attention_score_2", "attention_score_3", "attention_score_4", "attention_score_5", "attention_score_6",
    "recognition_score_1", "recognition_score_2", "recognition_score_3", "recognition_score_4", "recognition_score_5", "recognition_score_6",
    "routine_score_1", "routine_score_2", "routine_score_3", "routine_score_4", "routine_score_5", "routine_score_6",
    "pattern_score_1", "pattern_score_2", "pattern_score_3", "pattern_score_4", "pattern_score_5", "pattern_score_6",
    "last_game",
    "sessions_completed",
    "memory_recent_avg",
    "attention_recent_avg",
    "recognition_recent_avg",
    "routine_recent_avg",
    "pattern_recent_avg",
    "memory_trend",
    "attention_trend",
    "recognition_trend",
    "routine_trend",
    "pattern_trend",
    "memory_history_count",
    "attention_history_count",
    "recognition_history_count",
    "routine_history_count",
    "pattern_history_count",
]

class ModelService:
    _instance = None

    def __init__(self, model_path: str = "cogniva_model1_xgboost.pkl"):
        self.model_path = model_path
        self.pipeline = None
        self.target_encoder = None
        self.is_loaded = False
        self.load_model()

    @classmethod
    def get_instance(cls, model_path: str = "cogniva_model1_xgboost.pkl") -> "ModelService":
        if cls._instance is None:
            cls._instance = cls(model_path=model_path)
        return cls._instance

    def load_model(self):
        # Check model file path
        if not os.path.exists(self.model_path):
            # Check parent directories
            parent_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), "cogniva_model1_xgboost.pkl")
            if os.path.exists(parent_path):
                self.model_path = parent_path
            else:
                raise FileNotFoundError(f"Model file not found at '{self.model_path}' or '{parent_path}'")

        logger.info(f"Loading Model 1 from {self.model_path}...")
        try:
            saved_dict = joblib.load(self.model_path)
            if not isinstance(saved_dict, dict) or "model" not in saved_dict or "target_encoder" not in saved_dict:
                raise ValueError("Loaded pickle is not a valid Model 1 dict containing 'model' and 'target_encoder'")
            self.pipeline = saved_dict["model"]
            self.target_encoder = saved_dict["target_encoder"]
            self.is_loaded = True
            logger.info(f"Model 1 loaded successfully! Target classes: {list(self.target_encoder.classes_)}")
        except Exception as e:
            self.is_loaded = False
            logger.error(f"Failed to load Model 1: {e}", exc_info=True)
            raise RuntimeError(f"Failed to load Model 1 from {self.model_path}: {e}")

    def build_feature_vector(self, raw_input: Dict[str, Any]) -> pd.DataFrame:
        """Construct the exact 53-feature DataFrame required by Model 1."""
        # 1. Feature engineering
        engineered = calculate_cognitive_features(raw_input)

        # 2. Extract base fields
        age = float(raw_input.get("age", 70.0))
        initial_memory = float(raw_input.get("initial_memory", 50.0))
        initial_attention = float(raw_input.get("initial_attention", 50.0))
        initial_recognition = float(raw_input.get("initial_recognition", 50.0))
        initial_routine = float(raw_input.get("initial_routine", 50.0))
        initial_pattern = float(raw_input.get("initial_pattern", 50.0))
        
        last_game = str(raw_input.get("last_game", "none")).lower()
        if last_game not in ["memory", "attention", "recognition", "routine", "pattern", "none"]:
            last_game = "none"

        sessions_completed = int(raw_input.get("sessions_completed", 0))

        record: Dict[str, Any] = {
            "age": age,
            "initial_memory": initial_memory,
            "initial_attention": initial_attention,
            "initial_recognition": initial_recognition,
            "initial_routine": initial_routine,
            "initial_pattern": initial_pattern,
        }

        # 3. Add 30 raw score slots
        for domain in DOMAINS:
            for slot in range(1, 7):
                key = f"{domain}_score_{slot}"
                val = raw_input.get(key, -1)
                try:
                    record[key] = float(val) if val is not None and not (isinstance(val, float) and np.isnan(val)) else -1.0
                except (ValueError, TypeError):
                    record[key] = -1.0

        record["last_game"] = last_game
        record["sessions_completed"] = sessions_completed

        # 4. Add engineered features
        record.update(engineered)

        # 5. Build DataFrame in exact order
        df = pd.DataFrame([record])
        df = df[EXPECTED_FEATURES]

        # Verify no NaN values
        if df.isna().any().any():
            df = df.fillna(-1.0)

        return df

    def predict(self, raw_input: Dict[str, Any]) -> Tuple[str, int]:
        """Predict the next recommended cognitive domain / game.
        
        Returns:
            (predicted_domain_name, predicted_class_code)
        """
        if not self.is_loaded or self.pipeline is None:
            self.load_model()

        feature_df = self.build_feature_vector(raw_input)
        
        # Run prediction through sklearn Pipeline
        preds = self.pipeline.predict(feature_df)
        pred_code = int(preds[0])
        pred_domain = str(self.target_encoder.inverse_transform([pred_code])[0])

        return pred_domain, pred_code
