"""
Test cases cho chức năng xử lý dữ liệu thiếu trong CKD Prediction
"""

import requests
import json

# URL của API
API_BASE_URL = "http://localhost:8086/api/v1/analysis"

def test_complete_data():
    """Test với dữ liệu đầy đủ - sẽ sử dụng primary model"""
    
    patient_data = {
        # Essential data
        "serum_creatinine": 2.5,
        "gfr": 45.0,
        "physical_activity": "rarely",
        
        # Complete optional data
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
        "diet": "high protein",
        "smoking": "yes",
        "alcohol": "daily",
        "painkiller_usage": "yes",
        "family_history": "yes",
        "weight_changes": "loss",
        "stress_level": "high"
    }
    
    print("=== Test với dữ liệu đầy đủ ===")
    make_prediction_request(patient_data, "Complete Data")

def test_minimal_data():
    """Test với dữ liệu tối thiểu - sẽ sử dụng enhanced model"""
    
    patient_data = {
        # Chỉ có essential data
        "serum_creatinine": 2.5,
        "gfr": 45.0,
        "physical_activity": "rarely"
    }
    
    print("\n=== Test với dữ liệu tối thiểu ===")
    make_prediction_request(patient_data, "Minimal Data")

def test_partial_data():
    """Test với dữ liệu một phần - sẽ sử dụng enhanced model"""
    
    patient_data = {
        # Essential data
        "serum_creatinine": 3.2,
        "gfr": 30.0,
        "physical_activity": "weekly",
        
        # Một số optional data
        "bun": 35.0,
        "blood_pressure": 160.0,
        "smoking": "yes",
        "family_history": "yes",
        "stress_level": "high"
        # Thiếu: serum_calcium, ana, c3_c4, hematuria, oxalate_levels, 
        # urine_ph, water_intake, months, cluster, diet, alcohol, 
        # painkiller_usage, weight_changes
    }
    
    print("\n=== Test với dữ liệu một phần ===")
    make_prediction_request(patient_data, "Partial Data")

def test_no_essential_data():
    """Test khi thiếu dữ liệu essential - sẽ báo lỗi 422"""
    
    patient_data = {
        # Chỉ có physical_activity, thiếu serum_creatinine và gfr (essential)
        "physical_activity": "daily",
        "bun": 15.0,
        "blood_pressure": 120.0,
        "smoking": "no",
        "diet": "balanced"
        # Thiếu serum_creatinine và gfr - sẽ báo lỗi 422
    }
    
    print("\n=== Test khi thiếu dữ liệu essential ===")
    make_prediction_request(patient_data, "Missing Essential Data")

def test_missing_physical_activity():
    """Test khi thiếu physical_activity - sẽ báo lỗi 422"""
    
    patient_data = {
        # Có serum_creatinine và gfr nhưng thiếu physical_activity
        "serum_creatinine": 2.5,
        "gfr": 45.0,
        "bun": 15.0,
        "blood_pressure": 120.0,
        "smoking": "no",
        "diet": "balanced"
        # Thiếu physical_activity - sẽ báo lỗi 422
    }
    
    print("\n=== Test khi thiếu physical_activity ===")
    make_prediction_request(patient_data, "Missing Physical Activity")

def make_prediction_request(patient_data, test_name):
    """Gửi request dự đoán và in kết quả"""
    
    try:
        response = requests.post(
            f"{API_BASE_URL}/ckd-prediction",
            headers={"Content-Type": "application/json"},
            json=patient_data,
            timeout=30
        )
        
        if response.status_code == 200:
            result = response.json()
            
            print(f"✅ {test_name} - Thành công!")
            print(f"📊 Giai đoạn dự đoán: {result['predicted_stage']}")
            print(f"🎯 Độ tin cậy: {result['confidence']:.2%}")
            print(f"📝 Mô tả: {result['stage_description']}")
            print(f"⚠️ Mức độ nguy hiểm: {result['risk_level']}")
            
            print("📋 Khuyến nghị:")
            for i, rec in enumerate(result['recommendations'][:3], 1):
                print(f"   {i}. {rec}")
            
            if len(result['recommendations']) > 3:
                print(f"   ... và {len(result['recommendations']) - 3} khuyến nghị khác")
                
        else:
            print(f"❌ {test_name} - Lỗi {response.status_code}")
            print(f"📝 Chi tiết: {response.text}")
            
    except Exception as e:
        print(f"💥 {test_name} - Exception: {str(e)}")

def test_model_info():
    """Test API lấy thông tin về model"""
    
    try:
        response = requests.get(f"{API_BASE_URL}/ckd-model-info")
        
        if response.status_code == 200:
            result = response.json()
            print("\n=== Thông tin Model ===")
            print(json.dumps(result, indent=2, ensure_ascii=False))
        else:
            print(f"❌ Không thể lấy thông tin model - {response.status_code}")
            
    except Exception as e:
        print(f"💥 Lỗi khi lấy thông tin model: {str(e)}")

if __name__ == "__main__":
    print("🧪 Testing CKD Prediction API với dữ liệu thiếu")
    print("=" * 60)
    
    # Chạy các test cases
    test_complete_data()
    test_minimal_data()
    test_partial_data()
    test_no_essential_data()
    test_missing_physical_activity()
    
    # Test thông tin model
    test_model_info()
    
    print("\n" + "=" * 60)
    print("✨ Hoàn thành các test cases!")
