from typing import Dict, Any, Tuple
import math
import httpx

from app.models.ai_models import  DetailedTrendResponse, PredictResponse, TrendResponse, GetPredictHistoryResponse

class PredictService:
    def __init__(self):
        # In-memory storage, replace with database in production (giữ nguyên nếu bạn cần)
        self.chat_history = {}
        self.chat_routing = "http://localhost:8080/api/v1/predicts"

    # ========== PUBLIC API ==========

    async def get_predict_history(self, patientId: str) -> GetPredictHistoryResponse:
        """
        Gọi API Spring Boot để lấy lịch sử dự đoán (định dạng như bạn đã gửi)
        """
        url = f"{self.chat_routing}/get-predict/{patientId}"
        async with httpx.AsyncClient() as client:
            resp = await client.get(url)
            if resp.status_code == 200:
                data = resp.json()
                return GetPredictHistoryResponse(**data)
            return GetPredictHistoryResponse(statusCode=resp.status_code, message="Error", success=False, data=None)

    async def get_latest_trend(self, predictRes: PredictResponse) -> DetailedTrendResponse:
        """
        Tiện ích: fetch lịch sử rồi tính trend (so sánh stage giữa 2 PredictResponse)
        """
        history = await self.get_predict_history(predictRes.patientId)
        return self._build_trend_from_history(history, predictRes)

    # ========== CORE LOGIC ==========

    def _build_trend_from_history(self, history: GetPredictHistoryResponse, predictRes: PredictResponse) -> DetailedTrendResponse:
        """
        So sánh giai đoạn bệnh (stage) giữa history và predictRes
        Trả về DetailedTrendResponse với thông tin xu hướng giai đoạn bệnh
        """
        # Kiểm tra dữ liệu history
        if not history or not history.success or not history.data:
            return DetailedTrendResponse(
                trend=TrendResponse(
                    classification="INSUFFICIENT_HISTORY",
                    summary="Không có dữ liệu lịch sử để so sánh."
                )
            )

        # Lấy bản ghi từ history làm previous
        previous = history.data

        # Lấy stage từ previous và current (predictRes)
        stage_prev = getattr(previous, "stage", None)
        stage_curr = getattr(predictRes, "stage", None)
        
        # Lấy confidence từ previous và current
        conf_prev = getattr(previous, "confidence", None)
        conf_curr = getattr(predictRes, "confidence", None)
        conf_change = None
        if self._is_number(conf_prev) and self._is_number(conf_curr):
            conf_change = float(conf_curr) - float(conf_prev)

        # Xác định classification dựa trên stage
        if stage_prev is None or stage_curr is None:
            classification = "INSUFFICIENT_DATA"
            summary = "Thiếu thông tin giai đoạn bệnh để so sánh."
        elif stage_curr > stage_prev:
            classification = "WORSENING"
            summary = f"Giai đoạn bệnh tăng từ Stage {stage_prev} → Stage {stage_curr}."
        elif stage_curr < stage_prev:
            classification = "IMPROVING"
            summary = f"Giai đoạn bệnh giảm từ Stage {stage_prev} → Stage {stage_curr}."
        else:
            classification = "STABLE"
            summary = f"Giai đoạn bệnh ổn định ở Stage {stage_curr}."

        # Tạo TrendResponse
        trend = TrendResponse(
            classification=classification,
            stage_previous=stage_prev,
            stage_current=stage_curr,
            confidence_change=conf_change,
            summary=summary
        )

        # Trả về kết quả
        return DetailedTrendResponse(
            trend=trend
        )

    # ========== HELPERS ==========

    _NAME_MAP: Dict[str, Tuple[str, str]] = {
        # lowercased key -> (canonical_key, display_name)
        "gfr": ("GFR", "GFR"),
        "egrf": ("GFR", "GFR"),
        "gfr (glomerular filtration rate)": ("GFR", "GFR"),

        "serum creatinine": ("SERUM_CREATININE", "Serum Creatinine"),
        "serum_creatinine": ("SERUM_CREATININE", "Serum Creatinine"),

        "bun": ("BUN", "BUN"),
        "bun (blood urea nitrogen)": ("BUN", "BUN"),
        "blood urea nitrogen": ("BUN", "BUN"),

        "oxalate levels": ("OXALATE", "Oxalate Levels"),
        "oxalate_levels": ("OXALATE", "Oxalate Levels"),

        "serum calcium": ("CALCIUM", "Serum Calcium"),
        "serum_calcium": ("CALCIUM", "Serum Calcium"),

        "urine ph": ("URINE_PH", "Urine pH"),
        "urine_ph": ("URINE_PH", "Urine pH"),

        "blood pressure": ("BP", "Blood Pressure"),
        "huyết áp tâm trương": ("BP_DIASTOLIC", "Diastolic Blood Pressure"),
        "diastolic blood pressure": ("BP_DIASTOLIC", "Diastolic Blood Pressure"),
        "systolic blood pressure": ("BP_SYSTOLIC", "Systolic Blood Pressure"),
    }

    # hướng tốt/xấu: True = tăng là tốt, False = tăng là xấu (đối với diễn giải)
    _UP_IS_GOOD: Dict[str, bool] = {
        "GFR": True,
        "SERUM_CREATININE": False,
        "BUN": False,
        "OXALATE": False,
        "CALCIUM": False,     # tuỳ bối cảnh; đánh dấu False để thận trọng
        "URINE_PH": None,     # trung tính/tuỳ ngữ cảnh
        "BP": False,
        "BP_SYSTOLIC": False,
        "BP_DIASTOLIC": False,
    }

    # ===== string helpers =====

    def _canonical_match(self, canonical: str, display: str) -> bool:
        return self._canonical_from_display(display) == canonical

    def _canonical_from_display(self, display: str) -> str:
        key = (display or "").strip().lower()
        return self._NAME_MAP.get(key, (key.upper(), display))[0]

    def _to_canonical_display(self, display: str) -> str:
        key = (display or "").strip().lower()
        return self._NAME_MAP.get(key, (key.upper(), display))[0]

    @staticmethod
    def _is_number(x: Any) -> bool:
        try:
            float(x)
            return True
        except (TypeError, ValueError):
            return False
