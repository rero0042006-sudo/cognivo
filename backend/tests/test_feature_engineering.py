"""Unit Tests for Cogniva Model 1 Feature Engineering and Model Inference

Validates:
1. Zero scores [-1, -1, -1, -1, -1, -1] -> count=0, avg=-1, trend=-1
2. One score [0, -1, -1, -1, -1, -1] -> count=1, avg=0, trend=-1 (0 is a valid score)
3. Two scores [70, 65, -1, -1, -1, -1] -> count=2, avg=67.5, trend=-1
4. Five scores [60, 65, 70, 75, 80, -1] -> count=5, avg=70, trend=-1
5. Six scores [60, 65, 70, 75, 80, 85] -> count=6, avg=72.5, trend=15.0 (from PDF example)
6. Mixed domain history
7. Model 1 XGBoost inference with real sample from dataset
"""

import sys
import os
import pytest

# Ensure project root is in sys.path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from backend.feature_engineering import calculate_cognitive_features, DOMAINS
from backend.model_service import ModelService

def test_zero_scores():
    """Test 0 valid scores: all slots -1."""
    data = {f"memory_score_{i}": -1 for i in range(1, 7)}
    res = calculate_cognitive_features(data)
    assert res["memory_history_count"] == 0
    assert res["memory_recent_avg"] == -1.0
    assert res["memory_trend"] == -1.0

def test_one_score_with_zero():
    """Test 1 score where score is 0. 0 is a valid score, not a missing sentinel."""
    data = {
        "memory_score_1": 0,
        "memory_score_2": -1,
        "memory_score_3": -1,
        "memory_score_4": -1,
        "memory_score_5": -1,
        "memory_score_6": -1,
    }
    res = calculate_cognitive_features(data)
    assert res["memory_history_count"] == 1
    assert res["memory_recent_avg"] == 0.0
    assert res["memory_trend"] == -1.0

def test_two_scores():
    """Test 2 valid scores: [70, 65, -1, -1, -1, -1]. Avg should be 67.5, trend -1."""
    data = {
        "memory_score_1": 70,
        "memory_score_2": 65,
        "memory_score_3": -1,
        "memory_score_4": -1,
        "memory_score_5": -1,
        "memory_score_6": -1,
    }
    res = calculate_cognitive_features(data)
    assert res["memory_history_count"] == 2
    assert pytest.approx(res["memory_recent_avg"], 0.01) == 67.5
    assert res["memory_trend"] == -1.0

def test_five_scores():
    """Test 5 valid scores: [60, 65, 70, 75, 80, -1]. Avg should be 70, trend -1."""
    data = {
        "memory_score_1": 60,
        "memory_score_2": 65,
        "memory_score_3": 70,
        "memory_score_4": 75,
        "memory_score_5": 80,
        "memory_score_6": -1,
    }
    res = calculate_cognitive_features(data)
    assert res["memory_history_count"] == 5
    assert pytest.approx(res["memory_recent_avg"], 0.01) == 70.0
    assert res["memory_trend"] == -1.0

def test_six_scores_pdf_example():
    """Test PDF example: [60, 65, 70, 75, 80, 85].
    recent_avg = (60+65+70+75+80+85)/6 = 72.5
    trend = (75+80+85)/3 - (60+65+70)/3 = 80 - 65 = 15.0
    """
    data = {
        "memory_score_1": 60,
        "memory_score_2": 65,
        "memory_score_3": 70,
        "memory_score_4": 75,
        "memory_score_5": 80,
        "memory_score_6": 85,
    }
    res = calculate_cognitive_features(data)
    assert res["memory_history_count"] == 6
    assert pytest.approx(res["memory_recent_avg"], 0.01) == 72.5
    assert pytest.approx(res["memory_trend"], 0.01) == 15.0

def test_mixed_domains():
    """Test independent calculation across all 5 cognitive domains."""
    data = {
        "memory_score_1": 80,
        "memory_score_2": 90,
        "attention_score_1": 50,
        "recognition_score_1": 70,
        "recognition_score_2": 75,
        "recognition_score_3": 80,
        "recognition_score_4": 85,
        "recognition_score_5": 90,
        "recognition_score_6": 95,
        "routine_score_1": 40,
        "pattern_score_1": -1,
    }
    res = calculate_cognitive_features(data)
    assert res["memory_history_count"] == 2
    assert pytest.approx(res["memory_recent_avg"], 0.01) == 85.0
    assert res["memory_trend"] == -1.0

    assert res["attention_history_count"] == 1
    assert res["attention_recent_avg"] == 50.0

    assert res["recognition_history_count"] == 6
    assert pytest.approx(res["recognition_recent_avg"], 0.01) == 82.5
    assert pytest.approx(res["recognition_trend"], 0.01) == 15.0

    assert res["routine_history_count"] == 1
    assert res["routine_recent_avg"] == 40.0

    assert res["pattern_history_count"] == 0
    assert res["pattern_recent_avg"] == -1.0
    assert res["pattern_trend"] == -1.0

def test_model_service_prediction():
    """Test model loading and prediction with sample new user input."""
    model_svc = ModelService.get_instance()
    assert model_svc.is_loaded
    assert len(model_svc.target_encoder.classes_) == 5

    raw_input = {
        "age": 68,
        "initial_memory": 55.0,
        "initial_attention": 60.0,
        "initial_recognition": 70.0,
        "initial_routine": 45.0,
        "initial_pattern": 50.0,
        "last_game": "none",
        "sessions_completed": 0,
    }
    for d in DOMAINS:
        for s in range(1, 7):
            raw_input[f"{d}_score_{s}"] = -1.0

    domain, code = model_svc.predict(raw_input)
    assert domain in DOMAINS
    assert 0 <= code <= 4
