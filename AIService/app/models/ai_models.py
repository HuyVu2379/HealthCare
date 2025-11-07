from pydantic import BaseModel,Field
from typing import Optional, List, Dict, Any, Literal
from datetime import datetime
class ChatMessage(BaseModel):
    message: str
    user_id: Optional[str] = None
    group_id: Optional[str] = None
    summary: Optional[str] = None
    messages: List[str] = []

class ChatResponse(BaseModel):
    response: str
    timestamp: datetime
    confidence: Optional[float] = None
    sources: Optional[List[Dict[str, Any]]] = None
    num_sources: Optional[int] = None
    is_rag_response: Optional[bool] = False

class SimpleChatResponse(BaseModel):
    response: str
    confidence: Optional[float] = None

class HealthAnalysisRequest(BaseModel):
    symptoms: List[str]
    patient_age: Optional[int] = None
    patient_gender: Optional[str] = None
    medical_history: Optional[List[str]] = None
    vital_signs: Optional[Dict[str, Any]] = None

class GetSummaryResponse(BaseModel):
    summary: str
    messages: List[str]
class HealthAnalysisResponse(BaseModel):
    analysis: str
    recommendations: List[str]
    severity_level: str  # low, medium, high, critical
    confidence_score: float
    suggested_actions: List[str]

class CKDPredictionRequest(BaseModel):
    # Essential numerical features (luôn bắt buộc cho primary model, có thể thiếu cho enhanced model)
    serum_creatinine: Optional[float] = None
    gfr: Optional[float] = None

    # Optional numerical features (có thể thiếu)
    hematuria: Optional[int] = None  # 0 or 1
    bun: Optional[float] = None
    serum_calcium: Optional[float] = None
    ana: Optional[int] = None  # 0 or 1
    c3_c4: Optional[float] = None  # 0 or 1
    oxalate_levels: Optional[float] = None
    urine_ph: Optional[float] = None
    blood_pressure: Optional[float] = None
    water_intake: Optional[float] = None
    
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

class HealthMetricResponse(BaseModel):
    metricId: str
    patientId: str
    metricName: str
    metricValue: float
    unit: str
    medicalRecordId: Optional[str] = None
    measuredAt: str
class PredictResponse(BaseModel):
    predictId: str
    patientId: str
    stage: int
    recommendations: List[str]
    confidence: float
    # healthMetrics: List[HealthMetricResponse] = []
    createdAt: str
    updatedAt: str

class GetPredictHistoryResponse(BaseModel):
    statusCode: int
    message: str
    success: bool
    data: Optional[PredictResponse] = None


Classification = Literal["IMPROVING", "STABLE", "WORSENING", "INSUFFICIENT_HISTORY", "INSUFFICIENT_DATA"]
Status = Literal["WARNING", "NORMAL", "IMPROVING"]

class TrendResponse(BaseModel):
    classification: Classification
    stage_previous: Optional[int] = Field(None, alias="stagePrevious")
    stage_current: Optional[int] = Field(None, alias="stageCurrent")
    confidence_change: Optional[float] = Field(None, alias="confidenceChange")
    summary: str

    class Config:
        allow_population_by_field_name = True  # cho phép dùng snake_case khi tạo model
        populate_by_name = True                # (Pydantic v2)


class DetailedTrendResponse(BaseModel):
    trend: TrendResponse
    class Config:
        allow_population_by_field_name = True
        populate_by_name = True