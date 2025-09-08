from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from datetime import datetime

class ChatMessage(BaseModel):
    message: str
    user_id: Optional[str] = None
    session_id: Optional[str] = None

class ChatResponse(BaseModel):
    response: str
    session_id: str
    timestamp: datetime
    confidence: Optional[float] = None

class HealthAnalysisRequest(BaseModel):
    symptoms: List[str]
    patient_age: Optional[int] = None
    patient_gender: Optional[str] = None
    medical_history: Optional[List[str]] = None
    vital_signs: Optional[Dict[str, Any]] = None

class HealthAnalysisResponse(BaseModel):
    analysis: str
    recommendations: List[str]
    severity_level: str  # low, medium, high, critical
    confidence_score: float
    suggested_actions: List[str]

class MedicalImageAnalysisRequest(BaseModel):
    image_url: str
    image_type: str  # x-ray, mri, ct-scan, etc.
    patient_info: Optional[Dict[str, Any]] = None

class MedicalImageAnalysisResponse(BaseModel):
    findings: List[str]
    abnormalities: List[str]
    confidence_scores: Dict[str, float]
    recommendations: List[str]

class DrugInteractionRequest(BaseModel):
    medications: List[str]
    patient_age: Optional[int] = None
    patient_conditions: Optional[List[str]] = None

class DrugInteractionResponse(BaseModel):
    interactions: List[Dict[str, Any]]
    warnings: List[str]
    safe_combinations: List[str]
    recommendations: List[str]

class CKDPredictionRequest(BaseModel):
    # Essential numerical features (luôn bắt buộc cho primary model, có thể thiếu cho enhanced model)
    serum_creatinine: Optional[float] = None
    gfr: Optional[float] = None
    
    # Optional numerical features (có thể thiếu)
    bun: Optional[float] = None
    serum_calcium: Optional[float] = None
    ana: Optional[int] = None  # 0 or 1
    c3_c4: Optional[float] = None
    hematuria: Optional[int] = None  # 0 or 1
    oxalate_levels: Optional[float] = None
    urine_ph: Optional[float] = None
    blood_pressure: Optional[float] = None
    water_intake: Optional[float] = None
    months: Optional[int] = None
    cluster: Optional[int] = None
    
    # Essential categorical features (luôn bắt buộc cho primary model, có thể thiếu cho enhanced model)
    physical_activity: Optional[str] = None  # 'daily', 'weekly', 'rarely'
    
    # Optional categorical features (có thể thiếu)
    diet: Optional[str] = None  # 'balanced', 'high protein', 'low salt'
    smoking: Optional[str] = None  # 'yes', 'no'
    alcohol: Optional[str] = None  # 'never', 'occasionally', 'daily'
    painkiller_usage: Optional[str] = None  # 'yes', 'no'
    family_history: Optional[str] = None  # 'yes', 'no'
    weight_changes: Optional[str] = None  # 'stable', 'gain', 'loss'
    stress_level: Optional[str] = None  # 'low', 'moderate', 'high'

class CKDPredictionResponse(BaseModel):
    predicted_stage: int
    confidence: float
    stage_probabilities: Dict[str, float]
    stage_description: str
    recommendations: List[str]
    risk_level: str  # 'low', 'moderate', 'high', 'critical'
