from fastapi import APIRouter, HTTPException
from app.models.ai_models import (
    DetailedTrendResponse,
    HealthAnalysisRequest, 
    HealthAnalysisResponse,
    CKDPredictionRequest,
    CKDPredictionResponse,
    PredictResponse
)
from app.services.analysis_service import AnalysisService
from app.services.ckd_service import CKDPredictionService
from app.services.predict_service import PredictService
router = APIRouter()
analysis_service = AnalysisService()
ckd_service = CKDPredictionService()
predict = PredictService()
@router.post("/symptoms", response_model=HealthAnalysisResponse)
async def analyze_symptoms(request: HealthAnalysisRequest):
    """
    Analyze patient symptoms and provide health recommendations
    """
    try:
        analysis = await analysis_service.analyze_symptoms(request)
        return analysis
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/predict-current-trends", response_model=DetailedTrendResponse)
async def predict_current_trends(predictRes: PredictResponse):
    """
    Predict current health trends for a patient
    """
    try:
        trends = await predict.get_latest_trend(predictRes)
        return trends
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/ckd-prediction", response_model=CKDPredictionResponse)
async def predict_ckd_stage(request: CKDPredictionRequest):
    """
    Predict CKD (Chronic Kidney Disease) stage based on patient medical data
    
    This endpoint uses a machine learning model to predict the stage of chronic kidney disease
    based on various medical parameters including lab results, lifestyle factors, and patient history.
    
    CKD Stages:
    - Stage 0: Normal or high risk
    - Stage 1: Kidney damage with normal GFR (≥90)
    - Stage 2: Mild decrease in GFR (60-89)
    - Stage 3: Moderate decrease in GFR (30-59)
    - Stage 4: Severe decrease in GFR (15-29)
    - Stage 5: End-stage kidney disease (<15)
    """
    try:
        # Check if we have any data at all
        patient_data = {k: v for k, v in request.dict().items() if v is not None}
        if not patient_data:
            raise HTTPException(
                status_code=400, 
                detail="Cần cung cấp ít nhất một thông tin về bệnh nhân"
            )
        
        # Check for essential data for primary model (but allow enhanced model to handle missing data)
        essential_fields = ['serum_creatinine', 'gfr', 'physical_activity']
        missing_essential = [field for field in essential_fields 
                           if getattr(request, field) is None]
        
        # If missing all essential fields, return error
        if len(missing_essential) == len(essential_fields):
            raise HTTPException(
                status_code=422, 
                detail=[{
                    "type": "missing",
                    "loc": ["body", field],
                    "msg": "Field required",
                    "input": patient_data
                } for field in essential_fields]
            )
        
        # Validate input ranges
        warnings = ckd_service.validate_input_ranges(request)
        if warnings:
            raise HTTPException(
                status_code=400, 
                detail=f"Dữ liệu đầu vào không hợp lệ: {'; '.join(warnings)}"
            )
        
        # Make prediction
        prediction = await ckd_service.predict_ckd_stage(request)
        return prediction
        
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except HTTPException:
        # Re-raise HTTP exceptions
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# @router.get("/ckd-model-info")
# async def get_ckd_model_info():
#     """
#     Get detailed information about CKD prediction models and their capabilities
#     """
#     try:
#         model_info = ckd_service.get_model_info()
#         missing_data_info = ckd_service.get_missing_data_tolerance()
        
#         return {
#             "models": model_info,
#             "missing_data_handling": missing_data_info,
#             "usage_guidelines": {
#                 "essential_fields": [
#                     "serum_creatinine",
#                     "gfr", 
#                     "physical_activity"
#                 ],
#                 "recommendations": [
#                     "Cung cấp đầy đủ thông tin để có kết quả chính xác nhất",
#                     "Mô hình nâng cao sẽ tự động được sử dụng khi thiếu dữ liệu",
#                     "Độ tin cậy có thể giảm khi thiếu nhiều thông tin",
#                     "Luôn cung cấp ít nhất các thông tin bắt buộc"
#                 ]
#             }
#         }
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=str(e))

# @router.get("/ckd-info")
# async def get_ckd_info():
#     """
#     Get information about CKD stages and required input parameters
#     """
#     return {
#         "stages": {
#             0: "Bình thường hoặc nguy cơ cao - Thận hoạt động bình thường",
#             1: "Giai đoạn 1 - Tổn thương thận với GFR bình thường (≥90)",
#             2: "Giai đoạn 2 - Giảm GFR nhẹ (60-89)",
#             3: "Giai đoạn 3 - Giảm GFR vừa (30-59)",
#             4: "Giai đoạn 4 - Giảm GFR nặng (15-29)",
#             5: "Giai đoạn 5 - Suy thận giai đoạn cuối (<15)"
#         },
#         "required_parameters": {
#             "essential": {
#                 "serum_creatinine": "mg/dL - Mức creatinine trong máu",
#                 "gfr": "mL/min/1.73m² - Tỷ lệ lọc cầu thận",
#                 "physical_activity": "daily/weekly/rarely - Mức độ hoạt động thể chất"
#             },
#             "optional_numerical": {
#                 "bun": "mg/dL - Nitơ urê trong máu",
#                 "serum_calcium": "mg/dL - Canxi trong máu",
#                 "ana": "0/1 - Kháng thể kháng nhân",
#                 "c3_c4": "mg/dL - Bổ thể C3/C4",
#                 "hematuria": "0/1 - Có máu trong nước tiểu",
#                 "oxalate_levels": "mg/dL - Mức oxalate",
#                 "urine_ph": "4.5-8.5 - Độ pH nước tiểu",
#                 "blood_pressure": "mmHg - Huyết áp",
#                 "water_intake": "liters/day - Lượng nước uống",
#                 "months": "Thời gian theo dõi (tháng)",
#                 "cluster": "ID cụm phân loại"
#             },
#             "optional_categorical": {
#                 "diet": "balanced/high protein/low salt - Chế độ ăn",
#                 "smoking": "yes/no - Hút thuốc",
#                 "alcohol": "never/occasionally/daily - Sử dụng rượu",
#                 "painkiller_usage": "yes/no - Sử dụng thuốc giảm đau",
#                 "family_history": "yes/no - Tiền sử gia đình",
#                 "weight_changes": "stable/gain/loss - Thay đổi cân nặng",
#                 "stress_level": "low/moderate/high - Mức độ căng thẳng"
#             }
#         },
#         "model_info": {
#             "primary_model": "LightGBM (Optimized) - Độ chính xác cao với dữ liệu đầy đủ",
#             "enhanced_model": "Random Forest - Xử lý dữ liệu thiếu",
#             "automatic_selection": "Hệ thống tự động chọn model phù hợp",
#             "accuracy": "99% với dữ liệu đầy đủ",
#             "f1_score": 0.979
#         },
#         "features": {
#             "missing_data_handling": "Tự động xử lý dữ liệu thiếu",
#             "intelligent_model_selection": "Chọn model phù hợp tự động",
#             "confidence_adjustment": "Điều chỉnh độ tin cậy theo dữ liệu có sẵn",
#             "comprehensive_recommendations": "Khuyến nghị dựa trên tình trạng cụ thể"
#         }
#     }
