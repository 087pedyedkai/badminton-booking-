package com.example.badminton.model;

import java.time.LocalDate;

/**
 * คลาส Payment แทน "การรับเงินจริง 1 ครั้ง" พร้อมเลขอ้างอิงสลิป
 *
 * หลักการบัญชีของระบบนี้ (สำคัญ):
 * - รายได้ทุกบาทในรายงานมาจาก Payment เท่านั้น (นับเฉพาะเงินที่รับจริง)
 * - วันที่ของรายได้ = paymentDate (วันที่รับเงิน) ไม่ใช่วันที่ใช้สนาม
 *   เช่น วันนี้รับเงินจองสนามของวันเสาร์หน้า -> นับเป็นรายได้ของวันนี้
 * - การจองที่ใช้ชั่วโมงแพ็กเกจ "ไม่สร้าง Payment" เพราะเงินค่าแพ็กเกจถูกนับไปแล้วตอนซื้อแพ็กเกจ
 * - Payment หนึ่งรายการอ้างอิงรายการต้นทาง 1 รายการ (referenceId เช่น B001, PK001, R001, S001)
 *   และ PaymentService ไม่ยอมให้บันทึกหมวดเดียวกันของรายการเดิมซ้ำ จึงไม่มีการนับเงินซ้ำ
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - PaymentService สร้างและเก็บ Payment ทั้งหมด
 * - IncomeReport (รายงานรายได้) อ่าน Payment มาสรุปยอด
 */
public class Payment {

    private final String paymentId;
    private final LocalDate paymentDate;
    private final PaymentType type;
    private final double amount;
    private final Member member;          // อาจเป็น null ได้ (เช่น ลูกค้าทั่วไปซื้ออุปกรณ์)
    private final String referenceId;     // รหัสรายการต้นทาง
    private final String slipReference;   // เลขอ้างอิงสลิป / ที่อยู่ไฟล์สลิป / "CASH"

    public Payment(String paymentId, LocalDate paymentDate, PaymentType type, double amount,
                   Member member, String referenceId, String slipReference) {
        if (amount <= 0) {
            throw new IllegalArgumentException("จำนวนเงินที่ชำระต้องมากกว่า 0");
        }
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
        this.type = type;
        this.amount = amount;
        this.member = member;
        this.referenceId = referenceId;
        this.slipReference = slipReference;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public PaymentType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public Member getMember() {
        return member;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getSlipReference() {
        return slipReference;
    }

    @Override
    public String toString() {
        String memberText = (member == null) ? "ลูกค้าทั่วไป" : member.getMemberId();
        return paymentId + " | " + paymentDate + " | " + type.getThaiName()
                + " | อ้างอิง " + referenceId + " | " + memberText
                + " | " + String.format("%,.2f บาท", amount) + " | สลิป: " + slipReference;
    }
}
