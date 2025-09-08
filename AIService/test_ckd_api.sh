# CKD Prediction API Test Scripts

# Test 1: Get CKD Information
curl -X GET "http://localhost:8000/api/analysis/ckd-info" \
     -H "accept: application/json"

echo -e "\n\n=== Test CKD Prediction ==="

# Test 2: Predict CKD Stage - Normal Case
curl -X POST "http://localhost:8000/api/analysis/ckd-prediction" \
     -H "accept: application/json" \
     -H "Content-Type: application/json" \
     -d '{
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
     }'

echo -e "\n\n=== Test High Risk Case ==="

# Test 3: Predict CKD Stage - High Risk Case  
curl -X POST "http://localhost:8000/api/analysis/ckd-prediction" \
     -H "accept: application/json" \
     -H "Content-Type: application/json" \
     -d '{
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
     }'
