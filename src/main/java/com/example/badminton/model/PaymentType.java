package com.example.badminton.model;

/**
 * enum PaymentType บอก "หมวดหมู่ของรายได้" ของการชำระเงินแต่ละครั้ง
 * รายงานรายได้ (IncomeReport) จะใช้หมวดหมู่นี้แยกยอดเงินตามที่โจทย์กำหนด:
 * ค่าเช่าสนาม / ค่าแพ็กเกจ / ค่าเช่ารองเท้า / ขายอุปกรณ์
 *
 * enum คือชนิดข้อมูลที่มีค่าได้เฉพาะที่กำหนดไว้เท่านั้น จึงป้องกันการพิมพ์หมวดหมู่ผิด
 */
public enum PaymentType {
    COURT_RENTAL("ค่าเช่าสนาม"),
    PACKAGE_SALE("ขายแพ็กเกจชั่วโมง"),
    SHOE_RENTAL("ค่าเช่ารองเท้า"),
    EQUIPMENT_SALE("ขายอุปกรณ์แบดมินตัน");

    private final String thaiName;

    PaymentType(String thaiName) {
        this.thaiName = thaiName;
    }

    public String getThaiName() {
        return thaiName;
    }
}
