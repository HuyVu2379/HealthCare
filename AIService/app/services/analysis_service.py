from typing import Dict, Any, List
from app.models.ai_models import (
    HealthAnalysisRequest,
    HealthAnalysisResponse,
    MedicalImageAnalysisRequest,
    MedicalImageAnalysisResponse,
    DrugInteractionRequest,
    DrugInteractionResponse
)
import asyncio
import random

class AnalysisService:
    def __init__(self):
        # Initialize any AI models or external services here
        pass
    
    async def analyze_symptoms(self, request: HealthAnalysisRequest) -> HealthAnalysisResponse:
        """
        Analyze patient symptoms and provide health recommendations
        """
        try:
            # Simulate processing time
            await asyncio.sleep(0.5)
            
            # Simple symptom analysis logic (replace with actual AI)
            severity_level = self._assess_severity(request.symptoms)
            analysis = self._generate_symptom_analysis(request.symptoms)
            recommendations = self._generate_recommendations(request.symptoms)
            actions = self._suggest_actions(severity_level)
            
            return HealthAnalysisResponse(
                analysis=analysis,
                recommendations=recommendations,
                severity_level=severity_level,
                confidence_score=0.82,
                suggested_actions=actions
            )
            
        except Exception as e:
            raise Exception(f"Error analyzing symptoms: {str(e)}")
    
    async def analyze_medical_image(self, request: MedicalImageAnalysisRequest) -> MedicalImageAnalysisResponse:
        """
        Analyze medical images
        """
        try:
            # Simulate image processing
            await asyncio.sleep(1.0)
            
            # Placeholder analysis (replace with actual image AI)
            findings = self._analyze_image_type(request.image_type)
            
            return MedicalImageAnalysisResponse(
                findings=findings["findings"],
                abnormalities=findings["abnormalities"],
                confidence_scores=findings["confidence_scores"],
                recommendations=findings["recommendations"]
            )
            
        except Exception as e:
            raise Exception(f"Error analyzing medical image: {str(e)}")
    
    async def check_drug_interactions(self, request: DrugInteractionRequest) -> DrugInteractionResponse:
        """
        Check for drug interactions
        """
        try:
            await asyncio.sleep(0.3)
            
            interactions = self._check_medication_interactions(request.medications)
            
            return DrugInteractionResponse(
                interactions=interactions["interactions"],
                warnings=interactions["warnings"],
                safe_combinations=interactions["safe_combinations"],
                recommendations=interactions["recommendations"]
            )
            
        except Exception as e:
            raise Exception(f"Error checking drug interactions: {str(e)}")
    
    async def calculate_health_score(self, patient_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Calculate overall health score
        """
        try:
            await asyncio.sleep(0.2)
            
            # Simple health score calculation
            base_score = 75
            age = patient_data.get("age", 30)
            conditions = patient_data.get("conditions", [])
            
            # Adjust score based on age
            if age > 65:
                base_score -= 10
            elif age < 25:
                base_score += 5
            
            # Adjust score based on conditions
            base_score -= len(conditions) * 5
            
            # Ensure score is between 0 and 100
            final_score = max(0, min(100, base_score))
            
            return {
                "score": final_score,
                "category": self._get_health_category(final_score),
                "recommendations": self._get_health_recommendations(final_score)
            }
            
        except Exception as e:
            raise Exception(f"Error calculating health score: {str(e)}")
    
    def _assess_severity(self, symptoms: List[str]) -> str:
        """Assess symptom severity"""
        critical_symptoms = ["chest pain", "difficulty breathing", "severe bleeding", "đau ngực", "khó thở"]
        high_symptoms = ["high fever", "severe headache", "sốt cao", "đau đầu dữ dội"]
        
        for symptom in symptoms:
            if any(critical in symptom.lower() for critical in critical_symptoms):
                return "critical"
            elif any(high in symptom.lower() for high in high_symptoms):
                return "high"
        
        return "medium" if len(symptoms) > 3 else "low"
    
    def _generate_symptom_analysis(self, symptoms: List[str]) -> str:
        """Generate symptom analysis"""
        return f"Phân tích {len(symptoms)} triệu chứng: {', '.join(symptoms)}. Cần theo dõi và có thể cần tham khảo ý kiến bác sĩ."
    
    def _generate_recommendations(self, symptoms: List[str]) -> List[str]:
        """Generate recommendations based on symptoms"""
        recommendations = [
            "Nghỉ ngơi đầy đủ",
            "Uống nhiều nước",
            "Theo dõi triệu chứng",
            "Tham khảo ý kiến bác sĩ nếu triệu chứng không cải thiện"
        ]
        return recommendations[:3]  # Return top 3 recommendations
    
    def _suggest_actions(self, severity_level: str) -> List[str]:
        """Suggest actions based on severity"""
        if severity_level == "critical":
            return ["Đến cấp cứu ngay lập tức", "Gọi 115", "Không trì hoãn"]
        elif severity_level == "high":
            return ["Liên hệ bác sĩ trong ngày", "Theo dõi sát triệu chứng", "Chuẩn bị đến bệnh viện"]
        elif severity_level == "medium":
            return ["Đặt lịch khám bác sĩ", "Theo dõi triệu chứng", "Nghỉ ngơi"]
        else:
            return ["Tự chăm sóc tại nhà", "Theo dõi", "Tham khảo bác sĩ nếu cần"]
    
    def _analyze_image_type(self, image_type: str) -> Dict[str, Any]:
        """Analyze medical image (placeholder)"""
        return {
            "findings": [f"Hình ảnh {image_type} cho thấy cấu trúc bình thường"],
            "abnormalities": [],
            "confidence_scores": {"normal_structure": 0.85},
            "recommendations": ["Theo dõi định kỳ", "Tham khảo ý kiến bác sĩ chuyên khoa"]
        }
    
    def _check_medication_interactions(self, medications: List[str]) -> Dict[str, Any]:
        """Check medication interactions (placeholder)"""
        return {
            "interactions": [],
            "warnings": ["Luôn tham khảo ý kiến dược sĩ hoặc bác sĩ"],
            "safe_combinations": medications,
            "recommendations": ["Uống thuốc đúng giờ", "Không tự ý thay đổi liều dùng"]
        }
    
    def _get_health_category(self, score: int) -> str:
        """Get health category based on score"""
        if score >= 80:
            return "Excellent"
        elif score >= 60:
            return "Good"
        elif score >= 40:
            return "Fair"
        else:
            return "Poor"
    
    def _get_health_recommendations(self, score: int) -> List[str]:
        """Get health recommendations based on score"""
        if score >= 80:
            return ["Duy trì lối sống lành mạnh", "Kiểm tra sức khỏe định kỳ"]
        elif score >= 60:
            return ["Cải thiện chế độ ăn uống", "Tăng cường vận động", "Kiểm tra sức khỏe thường xuyên"]
        else:
            return ["Tham khảo ý kiến bác sĩ", "Thay đổi lối sống", "Theo dõi sức khỏe chặt chẽ"]
