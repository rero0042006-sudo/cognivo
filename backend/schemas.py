"""Pydantic Request and Response Schemas for Cogniva Recommendation Backend"""

from typing import Any, Dict, List, Optional
from pydantic import BaseModel, Field

class NextGameResponse(BaseModel):
    patient_id: str = Field(..., description="Unique patient identifier")
    next_game: str = Field(..., description="Predicted next cognitive domain / game (memory, attention, recognition, routine, pattern)")
    predicted_class_code: int = Field(..., description="Numeric class code from Model 1 XGBoost")
    sessions_completed: int = Field(..., description="Total completed game sessions")
    last_game: str = Field(..., description="Most recently completed game domain")
    engineered_features: Optional[Dict[str, Any]] = Field(default=None, description="Feature engineering summaries")

class DirectPredictionRequest(BaseModel):
    age: float = Field(default=70.0, description="Patient age")
    initial_memory: float = Field(default=50.0, description="Initial memory assessment score")
    initial_attention: float = Field(default=50.0, description="Initial attention assessment score")
    initial_recognition: float = Field(default=50.0, description="Initial recognition assessment score")
    initial_routine: float = Field(default=50.0, description="Initial routine assessment score")
    initial_pattern: float = Field(default=50.0, description="Initial pattern assessment score")
    last_game: str = Field(default="none", description="Last completed game domain (memory, attention, recognition, routine, pattern, none)")
    sessions_completed: int = Field(default=0, description="Total completed sessions")
    memory_scores: List[float] = Field(default_factory=lambda: [-1.0]*6, description="6 most recent memory scores (-1 for missing)")
    attention_scores: List[float] = Field(default_factory=lambda: [-1.0]*6, description="6 most recent attention scores (-1 for missing)")
    recognition_scores: List[float] = Field(default_factory=lambda: [-1.0]*6, description="6 most recent recognition scores (-1 for missing)")
    routine_scores: List[float] = Field(default_factory=lambda: [-1.0]*6, description="6 most recent routine scores (-1 for missing)")
    pattern_scores: List[float] = Field(default_factory=lambda: [-1.0]*6, description="6 most recent pattern scores (-1 for missing)")

class HealthResponse(BaseModel):
    status: str
    model_loaded: bool
    target_classes: List[str]

class PatientCreateRequest(BaseModel):
    id: str = Field(..., description="Unique patient identifier")
    full_name: str = Field(default="Patient", description="Full name")
    age: Optional[int] = Field(default=None, description="Patient age")
    date_of_birth: Optional[str] = Field(default=None, description="YYYY-MM-DD")
    gender: Optional[str] = Field(default="Female", description="Gender")
    email: Optional[str] = Field(default=None, description="Email address")
    phone: Optional[str] = Field(default=None, description="Phone number")
    is_completed: bool = Field(default=True, description="Whether profile is completed")

class ActivityCreateRequest(BaseModel):
    id: str = Field(..., description="Unique activity UUID")
    patient_id: str = Field(..., description="Unique patient ID")
    activity_name: str = Field(..., description="Name of the game or activity")
    score: float = Field(default=0.9, description="Score achieved (fraction 0-1 or percentage)")
    category: str = Field(default="Memory", description="Cognitive category")

class CaregiverCreateRequest(BaseModel):
    id: str = Field(..., description="Unique caregiver identifier")
    full_name: str = Field(default="Caregiver", description="Full name")
    email: Optional[str] = Field(default=None, description="Email address")
    phone: Optional[str] = Field(default=None, description="Phone number")
    patient_relationship: Optional[str] = Field(default="Family", description="Relationship to patient")
    linked_patient_id: Optional[str] = Field(default=None, description="Linked patient identifier")
    is_completed: bool = Field(default=True, description="Whether profile is completed")

