"""
Demo script cho tính năng xử lý dữ liệu thiếu trong CKD Prediction
"""

import asyncio
import sys
import os

# Add the AIService directory to the Python path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.services.ckd_service import CKDPredictionService
from app.models.ai_models import CKDPredictionRequest

async def demo_missing_data_handling():
    """Demo các tình huống xử lý dữ liệu thiếu"""
    
    print("🧪 DEMO: CKD Prediction với xử lý dữ liệu thiếu")
    print("=" * 60)
    
    # Khởi tạo service
    ckd_service = CKDPredictionService()
    
    # Demo 1: Dữ liệu đầy đủ
    print("\n📊 DEMO 1: Dữ liệu đầy đủ (Primary Model)")
    print("-" * 40)
    
    complete_data = CKDPredictionRequest(
        serum_creatinine=2.5,
        gfr=45.0,
        bun=25.0,
        serum_calcium=9.5,
        ana=0,
        c3_c4=120.0,
        hematuria=1,
        oxalate_levels=3.2,
        urine_ph=6.5,
        blood_pressure=140.0,
        water_intake=2.0,
        months=12,
        cluster=3,
        physical_activity="rarely",
        diet="high protein",
        smoking="yes",
        alcohol="daily",
        painkiller_usage="yes",
        family_history="yes",
        weight_changes="loss",
        stress_level="high"
    )
    
    try:
        result = await ckd_service.predict_ckd_stage(complete_data)
        print_prediction_result(result, "Complete Data")
    except Exception as e:
        print(f"❌ Lỗi: {e}")
    
    # Demo 2: Dữ liệu tối thiểu
    print("\n📊 DEMO 2: Dữ liệu tối thiểu (Enhanced Model)")
    print("-" * 40)
    
    minimal_data = CKDPredictionRequest(
        serum_creatinine=3.2,
        gfr=30.0,
        physical_activity="weekly"
    )
    
    try:
        result = await ckd_service.predict_ckd_stage(minimal_data)
        print_prediction_result(result, "Minimal Data")
    except Exception as e:
        print(f"❌ Lỗi: {e}")
    
    # Demo 3: Dữ liệu một phần
    print("\n📊 DEMO 3: Dữ liệu một phần (Enhanced Model)")
    print("-" * 40)
    
    partial_data = CKDPredictionRequest(
        serum_creatinine=4.0,
        gfr=25.0,
        bun=40.0,
        blood_pressure=160.0,
        physical_activity="rarely",
        smoking="yes",
        family_history="yes",
        stress_level="high"
    )
    
    try:
        result = await ckd_service.predict_ckd_stage(partial_data)
        print_prediction_result(result, "Partial Data")
    except Exception as e:
        print(f"❌ Lỗi: {e}")
    
    # Demo 4: Thông tin model
    print("\n📊 DEMO 4: Thông tin Models")
    print("-" * 40)
    
    model_info = ckd_service.get_model_info()
    print("🤖 Thông tin Models:")
    print(f"   Primary: {model_info['primary_model']['name']}")
    print(f"   Enhanced: {model_info['enhanced_model']['name']}")
    print(f"   Handles Missing Data: {model_info['enhanced_model']['handles_missing_data']}")
    
    missing_tolerance = ckd_service.get_missing_data_tolerance()
    print(f"\n📋 Tolerance for Missing Data:")
    print(f"   Max Missing Ratio: {missing_tolerance['max_missing_ratio']*100}%")
    print(f"   Max Missing Columns: {missing_tolerance['max_missing_columns']}")

def print_prediction_result(result, test_name):
    """In kết quả dự đoán một cách đẹp mắt"""
    
    print(f"✅ {test_name} - Kết quả dự đoán:")
    print(f"   🎯 Giai đoạn: {result.predicted_stage}")
    print(f"   📊 Độ tin cậy: {result.confidence:.1%}")
    print(f"   📝 Mô tả: {result.stage_description}")
    print(f"   ⚠️ Mức nguy hiểm: {result.risk_level.upper()}")
    
    print(f"   📋 Khuyến nghị chính:")
    for i, rec in enumerate(result.recommendations[:3], 1):
        print(f"      {i}. {rec}")
    
    if len(result.recommendations) > 3:
        print(f"      ... và {len(result.recommendations) - 3} khuyến nghị khác")

if __name__ == "__main__":
    asyncio.run(demo_missing_data_handling())
