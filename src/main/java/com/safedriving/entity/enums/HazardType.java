package com.safedriving.entity.enums;

/**
 * Phân loại đặc tính mối nguy hại của Điểm đen tai nạn (Accident Blackspot)
 */
public enum HazardType {
    ACCIDENT_PRONE,   // Đoạn đường có tần suất tai nạn cao (phản hồi từ tài xế & dữ liệu giao thông)
    BLIND_SPOT,       // Điểm mù, góc cua khuất tầm quan sát
    SHARP_CURVE,      // Khúc cua ngoằn ngoèo, gắt, bán kính cong nhỏ
    SLIPPERY_ROAD,    // Đoạn đường hay trơn trượt, bùn lầy hoặc dễ mất lái
    STEEP_SLOPE,      // Đèo dốc lớn, nguy cơ mất phanh
    OTHER             // Nguy hiểm khác
}
