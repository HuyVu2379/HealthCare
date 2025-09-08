"""
Demo đơn giản cho CKD Prediction API

Chạy file này để xem demo nhanh về cách API hoạt động.
"""

import json
from app.services.ckd_service import CKDPredictionService
from app.models.ai_models import CKDPredictionRequest

async def demo():
    """Demo function để test CKD prediction service"""
    
    print("🏥 Demo CKD Prediction API")
    print("=" * 60)
    
    # Khởi tạo service
    try:
        ckd_service = CKDPredictionService()
        print("✅ Model đã được tải thành công!")
    except Exception as e:
        print(f"❌ Lỗi tải model: {e}")
        return
    
    # Dữ liệu test cases
    test_cases = [
        {
            "name": "Bệnh nhân bình thường",
            "data": {
                "serum_creatinine": 1.0,
                "gfr": 95.0,
                "bun": 15.0,
                "serum_calcium": 10.0,
                "ana": 0,
                "c3_c4": 130.0,
                "hematuria": 0,
                "oxalate_levels": 2.0,
                "urine_ph": 7.0,
                "blood_pressure": 120.0,
                "water_intake": 2.5,
                "months": 6,
                "cluster": 1,
                "physical_activity": "daily",
                "diet": "balanced",
                "smoking": "no",
                "alcohol": "never",
                "painkiller_usage": "no",
                "family_history": "no",
                "weight_changes": "stable",
                "stress_level": "low"
            }
        },
        {
            "name": "Bệnh nhân nguy cơ cao",
            "data": {
                "serum_creatinine": 5.0,
                "gfr": 20.0,
                "bun": 60.0,
                "serum_calcium": 8.5,
                "ana": 1,
                "c3_c4": 100.0,
                "hematuria": 1,
                "oxalate_levels": 4.5,
                "urine_ph": 6.0,
                "blood_pressure": 160.0,
                "water_intake": 1.5,
                "months": 18,
                "cluster": 4,
                "physical_activity": "rarely",
                "diet": "high protein",
                "smoking": "yes",
                "alcohol": "daily",
                "painkiller_usage": "yes",
                "family_history": "yes",
                "weight_changes": "loss",
                "stress_level": "high"
            }
        }
    ]
    
    for i, test_case in enumerate(test_cases, 1):
        print(f"\n{'-' * 50}")
        print(f"🧪 Test Case {i}: {test_case['name']}")
        print(f"{'-' * 50}")
        
        try:
            # Tạo request object
            request = CKDPredictionRequest(**test_case['data'])
            
            # Validate input
            warnings = ckd_service.validate_input_ranges(request)
            if warnings:
                print(f"⚠️  Cảnh báo: {'; '.join(warnings)}")
            
            # Dự đoán
            result = await ckd_service.predict_ckd_stage(request)
            
            # Hiển thị kết quả
            print(f"🎯 Kết quả:")
            print(f"   Giai đoạn: {result.predicted_stage}")
            print(f"   Mô tả: {result.stage_description}")
            print(f"   Độ tin cậy: {result.confidence:.2%}")
            print(f"   Mức độ nguy cơ: {result.risk_level}")
            
            print(f"\n📊 Xác suất từng giai đoạn:")
            for stage, prob in result.stage_probabilities.items():
                print(f"   {stage}: {prob:.2%}")
            
            print(f"\n💡 Khuyến nghị ({len(result.recommendations)} mục):")
            for j, rec in enumerate(result.recommendations[:5], 1):  # Chỉ hiển thị 5 mục đầu
                print(f"   {j}. {rec}")
            if len(result.recommendations) > 5:
                print(f"   ... và {len(result.recommendations) - 5} khuyến nghị khác")
            
        except Exception as e:
            print(f"❌ Lỗi: {e}")
    
    print(f"\n{'=' * 60}")
    print("✅ Demo hoàn thành!")
    print("\n📚 Hướng dẫn sử dụng:")
    print("   1. Đọc file CKD_API_README.md")
    print("   2. Chạy API server: uvicorn main:app --reload")
    print("   3. Test API: python test_ckd_api.py")
    print("   4. Xem Swagger UI: http://localhost:8000/docs")

if __name__ == "__main__":
    import asyncio
    asyncio.run(demo())
