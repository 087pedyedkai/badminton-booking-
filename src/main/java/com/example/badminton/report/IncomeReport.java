package com.example.badminton.report;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Payment;
import com.example.badminton.model.PaymentType;
import com.example.badminton.util.FormatHelper;

import java.time.LocalDate;
import java.util.List;

/**
 * abstract class IncomeReport = "แม่แบบ" ของรายงานรายได้ ใช้ร่วมกันระหว่างรายวันและรายเดือน
 *
 * ทำไมเป็น abstract class?
 * - รายงานรายวันและรายเดือน "คำนวณเหมือนกันเกือบทั้งหมด" (แยกหมวด รวมยอด พิมพ์ผล)
 *   ต่างกันแค่ "ช่วงเวลา" ที่นับ และ "ชื่อหัวรายงาน"
 * - จึงเขียนส่วนที่เหมือนกันไว้ที่นี่ครั้งเดียว (Inheritance ลดโค้ดซ้ำ)
 * - ส่วนที่ต่างกันประกาศเป็น abstract method ให้คลาสลูกเขียนเอง (override):
 *     isInPeriod()      -> วันที่นี้อยู่ในช่วงรายงานหรือไม่
 *     getReportTitle()  -> ชื่อหัวรายงาน (มาจาก interface Reportable)
 * - สร้างออบเจกต์ของ IncomeReport ตรง ๆ ไม่ได้ ต้องสร้างผ่านคลาสลูกเท่านั้น
 *
 * กติกาการคิดรายได้ (Income rules):
 * 1) นับเฉพาะ "เงินที่รับจริง" = รายการ Payment เท่านั้น
 * 2) ใช้วันที่รับเงิน (paymentDate) เป็นตัวกำหนดว่ารายได้อยู่ในวัน/เดือนไหน
 * 3) การจองที่ใช้ชั่วโมงแพ็กเกจไม่มี Payment จึงไม่ถูกนับเป็นรายได้ซ้ำ
 *    (รายได้แพ็กเกจถูกนับครั้งเดียวตอนขาย ในหมวด "ขายแพ็กเกจชั่วโมง")
 * 4) Payment แต่ละรายการถูกวนนับเพียงครั้งเดียว และ PaymentService ไม่ยอมบันทึกรายการซ้ำ
 */
public abstract class IncomeReport implements Reportable {

    private final List<Payment> payments;
    private final List<Booking> bookings; // ใช้เพื่อแสดงข้อมูลชั่วโมงแพ็กเกจที่ถูกใช้ (ไม่ใช่รายได้)

    protected IncomeReport(List<Payment> payments, List<Booking> bookings) {
        this.payments = payments;
        this.bookings = bookings;
    }

    /** คลาสลูกต้องบอกว่า "วันที่นี้" อยู่ในช่วงของรายงานหรือไม่ */
    protected abstract boolean isInPeriod(LocalDate date);

    /** รวมรายได้ของหมวดที่กำหนด ภายในช่วงเวลาของรายงาน */
    public double getIncomeByType(PaymentType type) {
        double total = 0;
        for (Payment payment : payments) {
            if (payment.getType() == type && isInPeriod(payment.getPaymentDate())) {
                total += payment.getAmount();
            }
        }
        return total;
    }

    /** รายได้รวมทุกหมวด ภายในช่วงเวลาของรายงาน */
    public double getTotalIncome() {
        double total = 0;
        for (Payment payment : payments) {
            if (isInPeriod(payment.getPaymentDate())) {
                total += payment.getAmount();
            }
        }
        return total;
    }

    /** จำนวนรายการรับเงินในช่วงเวลา */
    public int getTransactionCount() {
        int count = 0;
        for (Payment payment : payments) {
            if (isInPeriod(payment.getPaymentDate())) {
                count++;
            }
        }
        return count;
    }

    /** ชั่วโมงแพ็กเกจที่ถูกใช้จอง (นับตามวันที่ทำรายการจอง) - แสดงเพื่อข้อมูลเท่านั้น ไม่ใช่รายได้ */
    public int getPackageHoursUsed() {
        int hours = 0;
        for (Booking booking : bookings) {
            if (booking.isUsedPackage() && isInPeriod(booking.getCreatedDate())) {
                hours += booking.getDurationHours();
            }
        }
        return hours;
    }

    /**
     * สร้างข้อความรายงาน (เมธอดนี้ใช้ร่วมกันทั้งรายวันและรายเดือน)
     * จุดที่เป็น Polymorphism: เรียก getReportTitle() และ isInPeriod() ซึ่งคลาสลูกแต่ละตัว override ไว้ต่างกัน
     * Java จะเลือกเมธอดของคลาสลูกที่แท้จริงให้อัตโนมัติขณะโปรแกรมทำงาน
     */
    @Override
    public String generateReport() {
        StringBuilder text = new StringBuilder();
        text.append("==================================================\n");
        text.append(getReportTitle()).append("\n");
        text.append("==================================================\n");
        // วนทุกหมวดรายได้ใน enum PaymentType แล้วแสดงยอดของแต่ละหมวด
        for (PaymentType type : PaymentType.values()) {
            text.append(String.format("%-25s : %s%n", type.getThaiName(), FormatHelper.money(getIncomeByType(type))));
        }
        text.append("--------------------------------------------------\n");
        text.append(String.format("%-25s : %s%n", "รายได้รวมทั้งสิ้น", FormatHelper.money(getTotalIncome())));
        text.append("จำนวนรายการรับเงิน : ").append(getTransactionCount()).append(" รายการ\n");
        text.append("ชั่วโมงแพ็กเกจที่ถูกใช้จองในช่วงนี้ : ").append(getPackageHoursUsed())
                .append(" ชม. (ไม่นับเป็นรายได้ซ้ำ เพราะนับไปแล้วตอนขายแพ็กเกจ)\n");
        text.append("==================================================");
        return text.toString();
    }
}
