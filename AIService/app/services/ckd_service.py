import pandas as pd
import numpy as np
import joblib
import json
import os
from typing import Dict, List
from app.models.ai_models import CKDPredictionRequest, CKDPredictionResponse

class CKDPredictionService:
    def __init__(self):
        # Primary model (requires complete data)
        self.model = None
        self.preprocessor = None
        self.metadata = None
        
        # Enhanced model (handles missing data)
        self.enhanced_model = None
        self.enhanced_preprocessor = None
        self.enhanced_metadata = None
        
        self.load_models()
    
    def load_models(self):
        """Load both CKD prediction models and preprocessors"""
        try:
            base_path = os.path.join(os.path.dirname(__file__), '..', 'data')
            
            # Load primary model
            model_path = os.path.join(base_path, 'best_ckd_model.pkl')
            self.model = joblib.load(model_path)
            
            # Load primary preprocessor
            preprocessor_path = os.path.join(base_path, 'ckd_preprocessor.pkl')
            self.preprocessor = joblib.load(preprocessor_path)
            
            # Load primary metadata
            metadata_path = os.path.join(base_path, 'model_metadata.json')
            with open(metadata_path, 'r', encoding='utf-8') as f:
                self.metadata = json.load(f)
            
            # Load enhanced model
            enhanced_model_path = os.path.join(base_path, 'enhanced_ckd_model.pkl')
            self.enhanced_model = joblib.load(enhanced_model_path)
            
            # Load enhanced preprocessor
            enhanced_preprocessor_path = os.path.join(base_path, 'enhanced_ckd_preprocessor.pkl')
            self.enhanced_preprocessor = joblib.load(enhanced_preprocessor_path)
            
            # Load enhanced metadata
            enhanced_metadata_path = os.path.join(base_path, 'enhanced_model_metadata.json')
            with open(enhanced_metadata_path, 'r', encoding='utf-8') as f:
                self.enhanced_metadata = json.load(f)
                
        except Exception as e:
            raise Exception(f"Error loading CKD models: {str(e)}")
    
    def check_data_completeness(self, input_df: pd.DataFrame, required_cols: List[str]) -> tuple:
        """Check if data is complete and determine which model to use"""
        missing_cols = [col for col in required_cols if col not in input_df.columns]
        missing_ratio = len(missing_cols) / len(required_cols)
        
        # If missing more than 20% of columns, use enhanced model
        use_enhanced = missing_ratio > 0.2 or len(missing_cols) > 4
        
        return missing_cols, use_enhanced
    
    def get_stage_description(self, stage: int) -> str:
        """Get description for CKD stage"""
        descriptions = {
            0: "Bình thường hoặc nguy cơ cao - Thận hoạt động bình thường",
            1: "Giai đoạn 1 - Tổn thương thận với GFR bình thường (≥90)",
            2: "Giai đoạn 2 - Giảm GFR nhẹ (60-89)",
            3: "Giai đoạn 3 - Giảm GFR vừa (30-59)",
            4: "Giai đoạn 4 - Giảm GFR nặng (15-29)",
            5: "Giai đoạn 5 - Suy thận giai đoạn cuối (<15)"
        }
        return descriptions.get(stage, f"Giai đoạn {stage}")
    
    def get_risk_level(self, stage: int, confidence: float) -> str:
        """Determine risk level based on stage and confidence"""
        if stage <= 1:
            return "low"
        elif stage == 2:
            return "moderate" if confidence > 0.7 else "low"
        elif stage == 3:
            return "high" if confidence > 0.7 else "moderate"
        else:  # stage 4 or 5
            return "critical"
    
    def get_recommendations(self, stage: int, patient_data: Dict) -> List[str]:
        """Get personalized recommendations based on CKD stage and patient data"""
        recommendations = []
        
        # General recommendations based on stage
        if stage == 0:
            recommendations.extend([
                "Duy trì lối sống lành mạnh",
                "Kiểm tra sức khỏe định kỳ",
                "Uống đủ nước mỗi ngày"
            ])
        elif stage == 1:
            recommendations.extend([
                "Theo dõi chức năng thận định kỳ",
                "Kiểm soát huyết áp nếu có",
                "Duy trì chế độ ăn cân bằng"
            ])
        elif stage == 2:
            recommendations.extend([
                "Kiểm tra chức năng thận 6 tháng/lần",
                "Hạn chế protein trong chế độ ăn",
                "Kiểm soát chặt chẽ huyết áp và đường huyết"
            ])
        elif stage == 3:
            recommendations.extend([
                "Khám chuyên khoa thận 3-6 tháng/lần",
                "Chế độ ăn ít protein, ít muối",
                "Theo dõi canxi và phospho trong máu",
                "Chuẩn bị cho liệu pháp thay thế thận"
            ])
        elif stage >= 4:
            recommendations.extend([
                "Khám chuyên khoa thận hàng tháng",
                "Chế độ ăn đặc biệt theo chỉ định",
                "Chuẩn bị cho chạy thận nhân tạo hoặc ghép thận",
                "Theo dõi sát sao các biến chứng"
            ])
        
        # Specific recommendations based on patient data
        if patient_data.get('smoking') == 'yes':
            recommendations.append("🚭 Bỏ thuốc lá ngay lập tức")
        
        if patient_data.get('alcohol') == 'daily':
            recommendations.append("🍺 Hạn chế rượu bia")
        
        if patient_data.get('physical_activity') == 'rarely':
            recommendations.append("🏃‍♂️ Tăng cường hoạt động thể chất")
        
        if patient_data.get('diet') == 'high protein':
            recommendations.append("🥗 Chuyển sang chế độ ăn ít protein")
        
        # Handle water_intake safely (could be None)
        water_intake = patient_data.get('water_intake')
        if water_intake is not None and water_intake < 2.0:
            recommendations.append("💧 Tăng lượng nước uống lên 2-3 lít/ngày")
        
        if patient_data.get('stress_level') == 'high':
            recommendations.append("🧘‍♂️ Quản lý stress bằng thiền định, yoga")
        
        if patient_data.get('painkiller_usage') == 'yes':
            recommendations.append("💊 Tránh thuốc giảm đau không cần thiết")
        
        return recommendations
    
    async def predict_ckd_stage(self, request: CKDPredictionRequest) -> CKDPredictionResponse:
        """Predict CKD stage for patient data"""
        try:
            # Convert request to dictionary, excluding None values
            patient_data = {k: v for k, v in request.dict().items() if v is not None}
            
            # Check if we have any data at all
            if not patient_data:
                raise Exception("Không có dữ liệu bệnh nhân để xử lý")
            
            # Create DataFrame from input data
            input_df = pd.DataFrame([patient_data])
            
            # Get required columns from primary model metadata
            required_cols = (self.metadata['feature_columns']['numerical'] + 
                           self.metadata['feature_columns']['categorical'])
            
            # Check data completeness and determine which model to use
            missing_cols, use_enhanced = self.check_data_completeness(input_df, required_cols)
            
            # Check for essential data
            essential_cols = ['serum_creatinine', 'gfr', 'physical_activity']
            missing_essential = [col for col in essential_cols if col not in input_df.columns]
            
            # Force enhanced model if missing essential data or too much data
            if missing_essential or use_enhanced:
                # Use enhanced model that handles missing data
                model = self.enhanced_model
                preprocessor = self.enhanced_preprocessor
                metadata = self.enhanced_metadata
                model_name = "Enhanced Model (Missing Data Handling)"
                
                # Get enhanced model required columns
                enhanced_required_cols = (metadata['feature_columns']['numerical'] + 
                                        metadata['feature_columns']['categorical'])
                
                # Fill missing columns with appropriate default values for enhanced model
                for col in enhanced_required_cols:
                    if col not in input_df.columns:
                        if col in metadata['feature_columns']['numerical']:
                            input_df[col] = np.nan  # Enhanced model will handle this
                        else:
                            input_df[col] = 'unknown'  # Enhanced model will handle this
                
                # Use enhanced model columns
                feature_cols = enhanced_required_cols
                use_enhanced = True
                
                # Log usage of enhanced model
                if missing_essential:
                    print(f"Using enhanced model due to missing essential data: {missing_essential}")
                else:
                    print(f"Using enhanced model due to missing columns: {missing_cols}")
                
            else:
                # Use primary model
                model = self.model
                preprocessor = self.preprocessor
                metadata = self.metadata
                model_name = "Primary Model"
                
                # Fill missing columns with defaults for primary model
                for col in required_cols:
                    if col not in input_df.columns:
                        if col in self.metadata['feature_columns']['numerical']:
                            # Use median values for missing numerical data
                            input_df[col] = self._get_default_numerical_value(col)
                        else:
                            # Use most common category for missing categorical data
                            input_df[col] = self._get_default_categorical_value(col)
                
                feature_cols = required_cols
            
            # Ensure we have all required columns
            for col in feature_cols:
                if col not in input_df.columns:
                    if col in metadata['feature_columns']['numerical']:
                        input_df[col] = np.nan if use_enhanced else self._get_default_numerical_value(col)
                    else:
                        input_df[col] = 'unknown' if use_enhanced else self._get_default_categorical_value(col)
            
            # Preprocess and predict
            input_processed = preprocessor.transform(input_df[feature_cols])
            prediction = model.predict(input_processed)[0]
            prediction_proba = model.predict_proba(input_processed)[0]
            
            # Calculate confidence (max probability)
            confidence = float(max(prediction_proba))
            
            # Adjust confidence if using enhanced model due to missing data
            total_missing = len(missing_cols) + len(missing_essential)
            if use_enhanced and total_missing > 0:
                confidence_penalty = min(0.2, total_missing * 0.03)
                confidence = max(0.5, confidence - confidence_penalty)
            
            # Create stage probabilities dictionary
            stage_probabilities = {
                f'stage_{stage}': float(prob) 
                for stage, prob in zip(metadata['target_classes'], prediction_proba)
            }
            
            # Get predictions details
            predicted_stage = int(prediction)
            stage_description = self.get_stage_description(predicted_stage)
            risk_level = self.get_risk_level(predicted_stage, confidence)
            recommendations = self.get_recommendations(predicted_stage, patient_data)
            
            # Add model information to recommendations if enhanced model was used
            all_missing = missing_cols + missing_essential
            if use_enhanced and len(all_missing) > 0:
                missing_info = f"ℹ️ Sử dụng mô hình nâng cao do thiếu {len(all_missing)} thông tin: {', '.join(all_missing[:3])}"
                if len(all_missing) > 3:
                    missing_info += f" và {len(all_missing) - 3} thông tin khác"
                recommendations.insert(0, missing_info)
                recommendations.insert(1, "⚠️ Kết quả có thể chính xác hơn nếu cung cấp đầy đủ thông tin")
            
            return CKDPredictionResponse(
                predicted_stage=predicted_stage,
                confidence=confidence,
                stage_probabilities=stage_probabilities,
                stage_description=stage_description,
                recommendations=recommendations,
                risk_level=risk_level
            )
            
        except Exception as e:
            raise Exception(f"Lỗi trong quá trình dự đoán CKD: {str(e)}")
    
    def _get_default_numerical_value(self, column: str) -> float:
        """Get default values for missing numerical columns"""
        defaults = {
            'serum_creatinine': 1.2,
            'gfr': 90.0,
            'bun': 15.0,
            'serum_calcium': 9.5,
            'ana': 0,
            'c3_c4': 1.0,
            'hematuria': 0,
            'oxalate_levels': 0.3,
            'urine_ph': 6.0,
            'blood_pressure': 120.0,
            'water_intake': 2.0,
            'months': 12,
            'cluster': 0
        }
        return defaults.get(column, 0.0)
    
    def _get_default_categorical_value(self, column: str) -> str:
        """Get default values for missing categorical columns"""
        defaults = {
            'physical_activity': 'weekly',
            'diet': 'balanced',
            'smoking': 'no',
            'alcohol': 'occasionally',
            'painkiller_usage': 'no',
            'family_history': 'no',
            'weight_changes': 'stable',
            'stress_level': 'moderate'
        }
        return defaults.get(column, 'unknown')
    
    def get_model_info(self) -> Dict:
        """Get information about available models and their capabilities"""
        return {
            "primary_model": {
                "name": self.metadata.get("model_name", "Primary Model"),
                "requires_complete_data": True,
                "accuracy": self.metadata.get("test_accuracy", "N/A"),
                "required_features": len(self.metadata['feature_columns']['numerical'] + 
                                       self.metadata['feature_columns']['categorical'])
            },
            "enhanced_model": {
                "name": self.enhanced_metadata.get("model_name", "Enhanced Model"),
                "handles_missing_data": self.enhanced_metadata.get("capabilities", {}).get("handles_missing_data", True),
                "missing_data_strategy": self.enhanced_metadata.get("capabilities", {}).get("missing_data_strategy", {}),
                "required_features": len(self.enhanced_metadata['feature_columns']['numerical'] + 
                                       self.enhanced_metadata['feature_columns']['categorical'])
            }
        }
    
    def get_missing_data_tolerance(self) -> Dict:
        """Get information about missing data tolerance"""
        return {
            "max_missing_ratio": 0.2,
            "max_missing_columns": 4,
            "enhanced_model_triggers": [
                "Thiếu hơn 20% tổng số features",
                "Thiếu nhiều hơn 4 features",
                "Dữ liệu không đầy đủ cho mô hình chính"
            ],
            "strategy": {
                "numerical_features": "Median imputation",
                "categorical_features": "Unknown category assignment"
            }
        }

    def validate_input_ranges(self, request: CKDPredictionRequest) -> List[str]:
        """Validate input data ranges and return warnings if any"""
        warnings = []
        
        # Validate numerical ranges (only if values are not None)
        try:
            if hasattr(request, 'serum_creatinine') and request.serum_creatinine is not None:
                if request.serum_creatinine < 0.5 or request.serum_creatinine > 15:
                    warnings.append("Creatinine ngoài phạm vi bình thường (0.5-15 mg/dL)")
            
            if hasattr(request, 'gfr') and request.gfr is not None:
                if request.gfr < 5 or request.gfr > 150:
                    warnings.append("GFR ngoài phạm vi bình thường (5-150 mL/min/1.73m²)")
            
            if hasattr(request, 'bun') and request.bun is not None:
                if request.bun < 5 or request.bun > 100:
                    warnings.append("BUN ngoài phạm vi bình thường (5-100 mg/dL)")
            
            if hasattr(request, 'blood_pressure') and request.blood_pressure is not None:
                if request.blood_pressure < 80 or request.blood_pressure > 200:
                    warnings.append("Huyết áp ngoài phạm vi bình thường (80-200 mmHg)")
            
            if hasattr(request, 'urine_ph') and request.urine_ph is not None:
                if request.urine_ph < 4.5 or request.urine_ph > 8.5:
                    warnings.append("pH nước tiểu ngoài phạm vi bình thường (4.5-8.5)")
        except Exception as e:
            # Skip validation if there's any comparison error
            pass
        
        # Validate categorical values (only if values are not None)
        valid_categories = {
            'physical_activity': ['daily', 'weekly', 'rarely'],
            'diet': ['balanced', 'high protein', 'low salt'],
            'smoking': ['yes', 'no'],
            'alcohol': ['never', 'occasionally', 'daily'],
            'painkiller_usage': ['yes', 'no'],
            'family_history': ['yes', 'no'],
            'weight_changes': ['stable', 'gain', 'loss'],
            'stress_level': ['low', 'moderate', 'high']
        }
        
        try:
            for field, valid_values in valid_categories.items():
                if hasattr(request, field):
                    value = getattr(request, field)
                    if value is not None and value not in valid_values:
                        warnings.append(f"{field} phải là một trong: {valid_values}")
        except Exception as e:
            # Skip validation if there's any error
            pass
        
        return warnings
