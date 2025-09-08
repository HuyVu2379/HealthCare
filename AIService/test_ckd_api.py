"""
Ví dụ sử dụng CKD Prediction API

File này chứa các ví dụ về cách gọi API dự đoán CKD với dữ liệu mẫu.
"""

import requests
import json

# URL của API (thay đổi theo cấu hình thực tế)
API_BASE_URL = "http://localhost:8086/api/v1/analysis"

def test_ckd_prediction():
    """Test CKD prediction API với dữ liệu mẫu"""
    
    # Dữ liệu mẫu của một bệnh nhân
    patient_data = {
        # Numerical features
        "serum_creatinine": 2.5,
        "gfr": 45.0,
        "bun": 25.0,
        "serum_calcium": 9.5,
        "ana": 0,
        "c3_c4": 120.0,
        "hematuria": 1,
        "oxalate_levels": 3.2,
        "urine_ph": 6.5,
        "blood_pressure": 140.0,
        "water_intake": 2.0,
        "months": 12,
        "cluster": 3,
        
        # Categorical features
        "physical_activity": "rarely",
        "diet": "high protein",
        "smoking": "yes",
        "alcohol": "daily",
        "painkiller_usage": "yes",
        "family_history": "yes",
        "weight_changes": "loss",
        "stress_level": "high"
    }
    
    try:
        # Gọi API
        response = requests.post(
            f"{API_BASE_URL}/ckd-prediction",
            headers={"Content-Type": "application/json"},
            json=patient_data
        )
        
        if response.status_code == 200:
            result = response.json()
            print("🎯 Kết quả dự đoán CKD:")
            print(f"   Giai đoạn: {result['predicted_stage']}")
            print(f"   Mô tả: {result['stage_description']}")
            print(f"   Độ tin cậy: {result['confidence']:.2%}")
            print(f"   Mức độ nguy cơ: {result['risk_level']}")
            print(f"\n📋 Xác suất từng giai đoạn:")
            for stage, prob in result['stage_probabilities'].items():
                print(f"   {stage}: {prob:.2%}")
            print(f"\n💡 Khuyến nghị:")
            for i, rec in enumerate(result['recommendations'], 1):
                print(f"   {i}. {rec}")
        else:
            print(f"❌ Lỗi API: {response.status_code}")
            print(f"   Chi tiết: {response.text}")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Lỗi kết nối: {e}")

def test_ckd_info():
    """Test API lấy thông tin về CKD"""
    try:
        response = requests.get(f"{API_BASE_URL}/ckd-info")
        
        if response.status_code == 200:
            info = response.json()
            print("📚 Thông tin về CKD:")
            print("\n🏥 Các giai đoạn CKD:")
            for stage, desc in info['stages'].items():
                print(f"   Giai đoạn {stage}: {desc}")
            
            print(f"\n🤖 Thông tin mô hình:")
            model_info = info['model_info']
            print(f"   Tên: {model_info['name']}")
            print(f"   Độ chính xác: {model_info['accuracy']:.1%}")
            print(f"   F1-score: {model_info['f1_score']:.3f}")
            
        else:
            print(f"❌ Lỗi API: {response.status_code}")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Lỗi kết nối: {e}")

def test_multiple_patients():
    """Test với nhiều bệnh nhân"""
    patients = [
        # Bệnh nhân 1: Giai đoạn đầu
        {
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
        },
        # Bệnh nhân 2: Giai đoạn nặng
        {
            "serum_creatinine": 8.0,
            "gfr": 12.0,
            "bun": 80.0,
            "serum_calcium": 8.0,
            "ana": 1,
            "c3_c4": 90.0,
            "hematuria": 1,
            "oxalate_levels": 5.0,
            "urine_ph": 5.5,
            "blood_pressure": 180.0,
            "water_intake": 1.0,
            "months": 24,
            "cluster": 5,
            "physical_activity": "rarely",
            "diet": "high protein",
            "smoking": "yes",
            "alcohol": "daily",
            "painkiller_usage": "yes",
            "family_history": "yes",
            "weight_changes": "loss",
            "stress_level": "high"
        }
    ]
    
    for i, patient in enumerate(patients, 1):
        print(f"\n{'='*50}")
        print(f"🏥 BỆNH NHÂN {i}")
        print(f"{'='*50}")
        
        try:
            response = requests.post(
                f"{API_BASE_URL}/ckd-prediction",
                headers={"Content-Type": "application/json"},
                json=patient
            )
            
            if response.status_code == 200:
                result = response.json()
                print(f"Giai đoạn dự đoán: {result['predicted_stage']}")
                print(f"Mức độ nguy cơ: {result['risk_level']}")
                print(f"Độ tin cậy: {result['confidence']:.2%}")
                print(f"Số khuyến nghị: {len(result['recommendations'])}")
            else:
                print(f"❌ Lỗi: {response.text}")
                
        except Exception as e:
            print(f"❌ Lỗi: {e}")

if __name__ == "__main__":
    print("🚀 Testing CKD Prediction API")
    print("="*60)
    
    # Test thông tin CKD
    print("\n1️⃣ Lấy thông tin CKD:")
    test_ckd_info()
    
    # Test dự đoán cho 1 bệnh nhân
    print(f"\n{'='*60}")
    print("2️⃣ Test dự đoán cho 1 bệnh nhân:")
    test_ckd_prediction()
    
    # Test với nhiều bệnh nhân
    print(f"\n{'='*60}")
    print("3️⃣ Test với nhiều bệnh nhân:")
    test_multiple_patients()
    
    print(f"\n{'='*60}")
    print("✅ Hoàn thành test!")
