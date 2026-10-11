package com.example.badminton.model;

/**
 * คลาส Court แทน "สนามแบดมินตัน 1 สนาม"
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - BookingService สร้าง Court ไว้ 12 ออบเจกต์ (สนามที่ 1 ถึง 12) ตอนเริ่มโปรแกรม
 * - Booking แต่ละรายการจะ "อ้างอิง" (reference) ไปยัง Court ที่ถูกจอง
 *   ทำให้รู้ว่าการจองนั้นเป็นของสนามไหน
 */
public class Court {

    /** ราคาค่าเช่าสนามมาตรฐานต่อชั่วโมง (ตามโจทย์ 180 บาท) ใช้ร่วมกันทุกสนามจึงเป็น static final */
    public static final double STANDARD_PRICE_PER_HOUR = 180.0;

    // Encapsulation: ฟิลด์เป็น private ภายนอกอ่านได้ผ่าน getter เท่านั้น และไม่มี setter
    // เพราะหมายเลขสนามไม่ควรถูกเปลี่ยนหลังจากสร้างแล้ว
    private final int courtNumber;

    public Court(int courtNumber) {
        if (courtNumber <= 0) {
            throw new IllegalArgumentException("หมายเลขสนามต้องมากกว่า 0");
        }
        this.courtNumber = courtNumber;
    }

    public int getCourtNumber() {
        return courtNumber;
    }

    @Override
    public String toString() {
        return "สนาม " + courtNumber;
    }
}
