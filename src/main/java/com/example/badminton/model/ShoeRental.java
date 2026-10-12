package com.example.badminton.model;

import java.time.LocalDate;

/**
 * คลาส ShoeRental แทน "การเช่ารองเท้าแบดมินตัน" 1 ครั้ง
 * ตามโจทย์: ค่าเช่า 50 บาท ต่อคู่ ต่อวันที่จอง
 *
 * ข้อแตกต่างระหว่าง "เช่ารองเท้า" กับ "ซื้อรองเท้า":
 * - เช่ารองเท้า = คลาสนี้ (ShoeRental) ผูกกับการจอง (Booking) และนับรายได้เป็นหมวด "ค่าเช่ารองเท้า"
 * - ซื้อรองเท้า = เป็นสินค้า (Product) ขายผ่าน Sale และนับรายได้เป็นหมวด "ขายอุปกรณ์"
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - ShoeRental อ้างอิง Booking ที่ใช้รองเท้า (การจอง 1 รายการ = ใช้สนาม 1 วัน)
 * - Booking เก็บรายการ ShoeRental ไว้ เพื่อรวมค่าเช่ารองเท้าเข้าไปในยอดชำระของการจอง
 * - PaymentService เป็นผู้สร้าง ShoeRental และบันทึกการชำระเงิน
 */
public class ShoeRental {

    public static final double PRICE_PER_PAIR_PER_DAY = 50.0;
    public static final int MAX_PAIRS = 20; // จำกัดจำนวนคู่ต่อครั้ง กันการพิมพ์ผิด

    private final String rentalId;
    private final Booking booking;
    private final int pairs;
    private final int rentalDays;       // จำนวนวันที่เช่า (การจอง 1 รายการ = 1 วัน)
    private final String slipReference;

    public ShoeRental(String rentalId, Booking booking, int pairs, int rentalDays, String slipReference) {
        if (pairs <= 0 || pairs > MAX_PAIRS) {
            throw new IllegalArgumentException("จำนวนรองเท้าต้องอยู่ระหว่าง 1 ถึง " + MAX_PAIRS + " คู่");
        }
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("จำนวนวันที่เช่าต้องมากกว่า 0");
        }
        this.rentalId = rentalId;
        this.booking = booking;
        this.pairs = pairs;
        this.rentalDays = rentalDays;
        this.slipReference = slipReference;
    }

    /**
     * สูตรคำนวณค่าเช่ารองเท้า = จำนวนคู่ x จำนวนวัน x 50 บาท
     * เป็น static เพื่อให้แคชเชียร์ "ดูราคาก่อน" ได้ โดยยังไม่ต้องสร้างรายการเช่าจริง
     */
    public static double calculateFee(int pairs, int days) {
        return pairs * days * PRICE_PER_PAIR_PER_DAY;
    }

    public double getRentalFee() {
        return calculateFee(pairs, rentalDays);
    }

    public String getRentalId() {
        return rentalId;
    }

    public Booking getBooking() {
        return booking;
    }

    public int getPairs() {
        return pairs;
    }

    /** วันที่ใช้รองเท้า = วันที่จองสนาม (อ่านจาก Booking เสมอ จึงตรงกันแม้การจองถูกเลื่อนวัน) */
    public LocalDate getRentalDate() {
        return booking.getBookingDate();
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public String getSlipReference() {
        return slipReference;
    }

    @Override
    public String toString() {
        return rentalId + " | " + pairs + " คู่ x " + rentalDays + " วัน | วันที่ใช้ " + getRentalDate()
                + " | " + String.format("%,.2f", getRentalFee()) + " บาท";
    }
}
