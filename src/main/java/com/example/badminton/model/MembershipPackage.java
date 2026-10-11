package com.example.badminton.model;

import java.time.LocalDate;

/**
 * คลาส MembershipPackage แทน "แพ็กเกจเหมาชั่วโมง" 1 ครั้งที่สมาชิกซื้อ
 * ตามโจทย์: ราคา 2,600 บาท ได้ 15 ชั่วโมง
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - Member 1 คน มีได้หลายแพ็กเกจ (เก็บใน List ของ Member) เช่น ซื้อครั้งที่สองก่อนชั่วโมงเดิมหมด
 * - PaymentService เป็นผู้สร้างแพ็กเกจเมื่อขาย และบันทึกรายได้ "ค่าแพ็กเกจ" เพียงครั้งเดียวตอนซื้อ
 * - เมื่อจองสนามแบบใช้แพ็กเกจ ระบบจะเรียก useHours() เพื่อหักชั่วโมง (ไม่มีการเก็บเงินซ้ำ)
 */
public class MembershipPackage {

    public static final double PACKAGE_PRICE = 2600.0; // ราคาแพ็กเกจ
    public static final int PACKAGE_HOURS = 15;        // จำนวนชั่วโมงที่ได้รับ

    private final String packageId;
    private final LocalDate purchaseDate;
    private final int totalHours;
    private int remainingHours; // ชั่วโมงคงเหลือ (ลดลงทุกครั้งที่ใช้จอง)

    public MembershipPackage(String packageId, LocalDate purchaseDate) {
        this.packageId = packageId;
        this.purchaseDate = purchaseDate;
        this.totalHours = PACKAGE_HOURS;
        this.remainingHours = PACKAGE_HOURS; // ซื้อใหม่ = ชั่วโมงเต็ม
    }

    /**
     * หักชั่วโมงแพ็กเกจ
     * Encapsulation: ภายนอกแก้ remainingHours ตรง ๆ ไม่ได้ ต้องผ่านเมธอดนี้
     * ซึ่งตรวจสอบก่อนเสมอว่า "ห้ามหักเกินชั่วโมงที่เหลือ" ชั่วโมงจึงไม่มีทางติดลบ
     */
    public void useHours(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("จำนวนชั่วโมงที่ใช้ต้องมากกว่า 0");
        }
        if (hours > remainingHours) {
            throw new IllegalArgumentException("ชั่วโมงแพ็กเกจคงเหลือไม่พอ (เหลือ " + remainingHours + " ชั่วโมง)");
        }
        remainingHours -= hours;
    }

    public String getPackageId() {
        return packageId;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public int getRemainingHours() {
        return remainingHours;
    }

    public int getUsedHours() {
        return totalHours - remainingHours;
    }

    @Override
    public String toString() {
        return packageId + " | ซื้อวันที่ " + purchaseDate + " | ใช้ไป " + getUsedHours()
                + " ชม. | คงเหลือ " + remainingHours + "/" + totalHours + " ชม.";
    }
}
