"""Cogniva Model 1 — Deterministic Feature Engineering

Calculates:
1. history_count: Count of valid scores (score >= 0) among the 6 slots.
2. recent_avg: Average of all valid scores in the 6 slots (or -1 if count == 0).
3. trend: average(scores 4..6) - average(scores 1..3) when count == 6 (or -1 if count < 6).
"""

from typing import Any, Dict, List
import numpy as np

DOMAINS = ["memory", "attention", "recognition", "routine", "pattern"]

def calculate_cognitive_features(data: Dict[str, Any]) -> Dict[str, float | int]:
    """Calculate recent averages, trends, and history counts for all 5 domains.
    
    Expected raw slot keys: {domain}_score_1 ... {domain}_score_6
    -1 means 'not played / unavailable'.
    0 is a valid score.
    """
    features: Dict[str, float | int] = {}

    for domain in DOMAINS:
        raw_scores = [
            data.get(f"{domain}_score_1", -1),
            data.get(f"{domain}_score_2", -1),
            data.get(f"{domain}_score_3", -1),
            data.get(f"{domain}_score_4", -1),
            data.get(f"{domain}_score_5", -1),
            data.get(f"{domain}_score_6", -1),
        ]

        # Valid scores are real numbers >= 0 (and not None or NaN)
        valid_scores: List[float] = []
        for s in raw_scores:
            if s is not None and not (isinstance(s, float) and np.isnan(s)):
                try:
                    val = float(s)
                    if val >= 0:
                        valid_scores.append(val)
                except (ValueError, TypeError):
                    pass

        # 1. history_count
        history_count = len(valid_scores)
        features[f"{domain}_history_count"] = int(history_count)

        # 2. recent_avg (average of all valid scores among the 6 slots)
        if history_count == 0:
            features[f"{domain}_recent_avg"] = -1.0
        else:
            features[f"{domain}_recent_avg"] = float(sum(valid_scores) / history_count)

        # 3. trend: (score_4 + score_5 + score_6)/3 - (score_1 + score_2 + score_3)/3
        # ONLY calculated when all 6 slots are valid
        if history_count == 6:
            prev_avg = (float(raw_scores[0]) + float(raw_scores[1]) + float(raw_scores[2])) / 3.0
            rec_avg = (float(raw_scores[3]) + float(raw_scores[4]) + float(raw_scores[5])) / 3.0
            features[f"{domain}_trend"] = float(rec_avg - prev_avg)
        else:
            features[f"{domain}_trend"] = -1.0

    return features
