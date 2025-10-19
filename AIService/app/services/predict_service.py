from typing import Dict, Any, List, Optional, Tuple
import math
import httpx

from app.models.ai_models import GetPredictHistoryResponse, DetailedTrendResponse, TrendResponse, MetricComparison

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
        url = f"{self.chat_routing}/get-predict-history/{patientId}"
        async with httpx.AsyncClient() as client:
            resp = await client.get(url)
            if resp.status_code == 200:
                data = resp.json()
                return GetPredictHistoryResponse(**data)
            return GetPredictHistoryResponse(statusCode=resp.status_code, message="Error", success=False, data=[])

    async def get_latest_trend(self, patientId: str) -> DetailedTrendResponse:
        """
        Tiện ích: fetch lịch sử rồi tính trend + metricComparisons (response rút gọn bạn muốn)
        """
        history = await self.get_predict_history(patientId)
        return self._build_trend_from_history(history)

    # ========== CORE LOGIC ==========

    def _build_trend_from_history(self, history: GetPredictHistoryResponse) -> DetailedTrendResponse:
        """
        Biến lịch sử dự đoán thành DetailedTrendResponse (trend + metricComparisons)
        - Lấy 2 bản ghi gần nhất: current vs previous
        - Chuẩn hoá metricName, ghép cặp, tính % thay đổi
        - Ưu tiên GFR cho summary; nếu thiếu GFR thì dùng Creatinine, nếu vẫn thiếu dùng BUN
        - Classification ưu tiên theo stage
        """
        # Trường hợp không có đủ dữ liệu
        if not history or not history.success or not history.data or len(history.data) < 1:
            return DetailedTrendResponse(
                trend=TrendResponse(
                    classification="INSUFFICIENT_HISTORY",
                    summary="Không có dữ liệu dự đoán."
                ),
                metric_comparisons=[]
            )

        # Sắp xếp theo createdAt/measuredAt giảm dần (ưu tiên createdAt nếu có)
        records = sorted(
            history.data,
            key=lambda r: (
                getattr(r, "createdAt", None) or getattr(r, "updatedAt", None) or "1970-01-01"
            ),
            reverse=True
        )

        current = records[0]
        previous = records[1] if len(records) > 1 else None

        if previous is None:
            return DetailedTrendResponse(
                trend=TrendResponse(
                    classification="INSUFFICIENT_HISTORY",
                    stage_previous=None,
                    stage_current=getattr(current, "stage", None),
                    confidence_change=None,
                    metric_previous=None,
                    metric_current=None,
                    metric_change_pct=None,
                    metric_name=None,
                    summary="Chưa đủ dữ liệu lịch sử (chỉ có 1 bản ghi)."
                ),
                metric_comparisons=[]
            )

        # Chuẩn hoá metric maps
        curr_map = self._normalize_metrics(getattr(current, "healthMetrics", []) or [])
        prev_map = self._normalize_metrics(getattr(previous, "healthMetrics", []) or [])

        # Danh sách metric ưu tiên cho phần summary
        summary_priority = ["GFR", "SERUM_CREATININE", "BUN"]

        # Tính so sánh cho tất cả metric trùng nhau
        comparisons: List[MetricComparison] = []
        for key, curr in curr_map.items():
            if key in prev_map:
                prev = prev_map[key]
                comp = self._compare_metric(
                    canonical_name=key,
                    display_name=curr["display"],        # tên hiển thị gọn gàng
                    previous_value=prev["value"],
                    current_value=curr["value"],
                    unit=curr.get("unit") or prev.get("unit") or ""
                )
                comparisons.append(comp)

        # Xác định stage/ confidence
        stage_prev = getattr(previous, "stage", None)
        stage_curr = getattr(current, "stage", None)
        conf_prev = getattr(previous, "confidence", None)
        conf_curr = getattr(current, "confidence", None)
        conf_change = None
        if self._is_number(conf_prev) and self._is_number(conf_curr):
            conf_change = float(conf_curr) - float(conf_prev)

        # Chọn metric cho summary
        summary_metric_name, summary_prev, summary_curr, summary_pct = self._pick_summary_metric(comparisons, summary_priority)

        # Xếp loại trend (ưu tiên Stage)
        classification = self._classify_trend(stage_prev, stage_curr, comparisons)

        # Xây dựng summary text
        summary_parts: List[str] = []
        if stage_prev is not None and stage_curr is not None and stage_prev != stage_curr:
            arrow = "→"
            summary_parts.append(f"Stage {'tăng' if stage_curr > stage_prev else 'giảm'} ({stage_prev} {arrow} {stage_curr}).")
        if summary_metric_name and summary_pct is not None:
            dir_word = "tăng" if (summary_curr - summary_prev) > 0 else "giảm"
            summary_parts.append(f"{summary_metric_name} {dir_word} ~{abs(round(summary_pct, 1))}% so với lần trước.")
        if not summary_parts:
            summary_parts.append("Các chỉ số chính ổn định so với lần trước.")

        trend = TrendResponse(
            classification=classification,
            stage_previous=stage_prev,
            stage_current=stage_curr,
            confidence_change=conf_change,
            metric_previous=summary_prev,
            metric_current=summary_curr,
            metric_change_pct=summary_pct,
            metric_name=summary_metric_name,
            summary=" ".join(summary_parts)
        )

        # Trả kết quả cuối
        return DetailedTrendResponse(
            trend=trend,
            metric_comparisons=comparisons
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

    def _normalize_metrics(self, raw_metrics: List[Any]) -> Dict[str, Dict[str, Any]]:
        """
        Chuẩn hoá danh sách metric thành dict: canonical_key -> {display, value, unit}
        Bỏ qua metric non-numeric (các categorical như 'low', 'weekly', ...)
        """
        out: Dict[str, Dict[str, Any]] = {}
        for m in raw_metrics:
            name_raw = getattr(m, "metricName", None) or (m.get("metricName") if isinstance(m, dict) else None)
            value = getattr(m, "metricValue", None) or (m.get("metricValue") if isinstance(m, dict) else None)
            unit = getattr(m, "unit", None) or (m.get("unit") if isinstance(m, dict) else "")

            if not name_raw or not self._is_number(value):
                continue

            key = str(name_raw).strip().lower()
            canonical_key, display = self._NAME_MAP.get(key, (key.upper(), name_raw))
            out[canonical_key] = {
                "display": display,
                "value": float(value),
                "unit": unit or ""
            }
        return out

    def _compare_metric(
        self,
        canonical_name: str,
        display_name: str,
        previous_value: float,
        current_value: float,
        unit: str
    ) -> MetricComparison:
        """
        Tính changePct, xác định status (NORMAL|WARNING|IMPROVING) và message ngắn
        Ngưỡng:
            - |Δ%| < 10%  => NORMAL
            - hướng tốt & |Δ%| >= 10% => IMPROVING
            - hướng xấu & |Δ%| >= 10% => WARNING
        """
        change_pct = None
        if self._is_number(previous_value) and float(previous_value) != 0.0:
            change_pct = (float(current_value) - float(previous_value)) / float(previous_value) * 100.0

        # Mặc định NORMAL nếu không tính được %
        status = "NORMAL"
        message = "Ổn định."

        if change_pct is not None:
            up_is_good = self._UP_IS_GOOD.get(canonical_name, None)
            direction_up = (current_value - previous_value) > 0
            abs_pct = abs(change_pct)

            # Định nghĩa status
            if abs_pct < 10:
                status = "NORMAL"
            else:
                if up_is_good is True and direction_up:
                    status = "IMPROVING"
                elif up_is_good is False and (not direction_up):
                    status = "IMPROVING"
                else:
                    status = "WARNING"

            # Sinh message
            if abs_pct < 1e-9:
                message = "Ổn định."
            else:
                dir_word = "tăng" if direction_up else "giảm"
                # Hậu tố tác động
                impact = ""
                if status == "IMPROVING":
                    impact = "— tín hiệu tích cực."
                elif status == "WARNING":
                    impact = "— cần theo dõi sát."
                else:
                    impact = "."

                message = f"{dir_word.capitalize()} ~{abs(round(change_pct, 1))}% so với lần trước {impact}"

        return MetricComparison(
            metric=display_name,
            previous_value=float(previous_value) if self._is_number(previous_value) else None,
            current_value=float(current_value) if self._is_number(current_value) else None,
            unit=unit or "",
            change_pct=change_pct,
            status=status,
            message=message
        )

    def _pick_summary_metric(
        self,
        comparisons: List[MetricComparison],
        priority: List[str]
    ) -> Tuple[Optional[str], Optional[float], Optional[float], Optional[float]]:
        """
        Chọn metric dùng cho phần summary (ưu tiên theo danh sách priority: GFR, Creatinine, BUN)
        Trả về: (display_name, prev, curr, change_pct)
        """
        # Map display_name -> comp
        comp_map = {self._to_canonical_display(c.metric): c for c in comparisons}
        for p in priority:
            # đối chiếu theo canonical
            for comp in comparisons:
                if self._canonical_match(p, comp.metric):
                    return comp.metric, comp.previous_value, comp.current_value, comp.change_pct

        # fallback: lấy metric đầu tiên nếu có
        if comparisons:
            c0 = comparisons[0]
            return c0.metric, c0.previous_value, c0.current_value, c0.change_pct

        return None, None, None, None

    def _classify_trend(self, stage_prev: Optional[int], stage_curr: Optional[int],
                        comparisons: List[MetricComparison]) -> str:
        """
        Phân loại tổng quát: ưu tiên Stage; nếu stage không đổi hoặc thiếu thì dựa đa số (GFR/Creatinine/BUN/Oxalate)
        """
        if stage_prev is not None and stage_curr is not None:
            if stage_curr > stage_prev:
                return "WORSENING"
            if stage_curr < stage_prev:
                return "IMPROVING"
            # = nhau, xét tiếp theo metrics
        # Bỏ phiếu theo các metric chính
        improving, worsening = 0, 0
        for comp in comparisons:
            name_canon = self._canonical_from_display(comp.metric)
            if name_canon in {"GFR", "SERUM_CREATININE", "BUN", "OXALATE"}:
                if comp.status == "IMPROVING":
                    improving += 1
                elif comp.status == "WARNING":
                    worsening += 1

        if improving > worsening:
            return "IMPROVING"
        if worsening > improving:
            return "WORSENING"
        return "STABLE"

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
