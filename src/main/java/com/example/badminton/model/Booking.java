package com.example.badminton.model;

import com.example.badminton.util.FormatHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * คลาส Booking แทน "การจองสนาม 1 รายการ"
 *
 * ข้อมูลที่โจทย์กำหนดให้บันทึก: รหัสการจอง, สมาชิก, หมายเลขสนาม, วันที่จอง,
 * เวลาเริ่ม, เวลาสิ้นสุด/จำนวนชั่วโมง, ค่าเช่ารวม, เลขอ้างอิงสลิป, สถานะการชำระเงิน
 *
 * ความสัมพันธ์กับคลาสอื่น (Association):
 * - Booking --> Member : รู้ว่าใครเป็นผู้จอง
 * - Booking --> Court  : รู้ว่าจองสนามไหน
 * - Booking --> ShoeRental (หลายรายการ) : ค่าเช่ารองเท้าที่ผูกกับการจองนี้
 * - BookingService เป็นผู้สร้าง Booking หลังจากตรวจสอบเวลาและสนามว่างแล้ว
 * - PaymentService บันทึกการชำระค่าสนามของ Booking นี้
 *
 * สมมติฐาน: การจองเป็นหน่วย "ชั่วโมงเต็ม" (เริ่มตรงชั่วโมง เช่น 13:00 และจองครั้งละ n ชั่วโมง)
 * เพื่อให้การหักชั่วโมงแพ็กเกจและการนับชั่วโมงที่ 20 สำหรับส่วนลดทำได้ชัดเจน
 */
public class Booking {

    // สถานะการชำระเงินของค่าสนาม (เก็บเป็นข้อความภาษาไทยเพื่อให้อ่านง่าย)
    public static final String STATUS_UNPAID = "รอชำระเงิน";
    public static final String STATUS_PAID = "ชำระเงินแล้ว";
    public static final String STATUS_PACKAGE = "ใช้ชั่วโมงแพ็กเกจ (ไม่ต้องชำระค่าสนาม)";

    private final String bookingId;
    private final Member member;
    private Court court;                    // แก้ไขได้ผ่าน reschedule() เท่านั้น
    private LocalDate bookingDate;
    private LocalTime startTime;
    private final int durationHours;
    private final double courtFee;          // ค่าสนามที่คำนวณแล้ว (รวมส่วนลดถ้ามี, ใช้แพ็กเกจ = 0)
    private final int discountedHours;      // จำนวนชั่วโมงในการจองนี้ที่ได้ส่วนลด 30%
    private final boolean usedPackage;      // true = จองด้วยชั่วโมงแพ็กเกจ
    private final LocalDate createdDate;    // วันที่ทำรายการจอง
    private String slipReference;           // เลขอ้างอิงสลิป หรือที่อยู่ไฟล์สลิป
    private String paymentStatus;
    private final List<ShoeRental> shoeRentals = new ArrayList<>();

    public Booking(String bookingId, Member member, Court court, LocalDate bookingDate,
                   LocalTime startTime, int durationHours, double courtFee,
                   int discountedHours, boolean usedPackage) {
        if (durationHours <= 0) {
            throw new IllegalArgumentException("จำนวนชั่วโมงต้องมากกว่า 0");
        }
        this.bookingId = bookingId;
        this.member = member;
        this.court = court;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.durationHours = durationHours;
        this.courtFee = courtFee;
        this.discountedHours = discountedHours;
        this.usedPackage = usedPackage;
        this.createdDate = LocalDate.now();
        this.slipReference = "-";
        // ถ้าใช้แพ็กเกจ ไม่มีค่าสนามต้องจ่าย จึงตั้งสถานะเป็น "ใช้แพ็กเกจ" ตั้งแต่แรก
        this.paymentStatus = usedPackage ? STATUS_PACKAGE : STATUS_UNPAID;
    }

    /** เวลาสิ้นสุด = เวลาเริ่ม + จำนวนชั่วโมง */
    public LocalTime getEndTime() {
        return startTime.plusHours(durationHours);
    }

    /**
     * ตรวจว่าการจองนี้ "ทับเวลา" กับช่วงเวลาที่ถามมาหรือไม่ (ใช้ป้องกันการจองซ้อน)
     * เงื่อนไขทับกัน: ต้องเป็นสนามเดียวกัน + วันเดียวกัน + ช่วงเวลาซ้อนกัน
     * ช่วงเวลา [A, B) กับ [C, D) ซ้อนกันเมื่อ A < D และ C < B
     * ตัวอย่าง: 13:00-15:00 กับ 15:00-16:00 "ไม่ทับ" (จบ 15:00 พอดีแล้วอีกคนเริ่ม 15:00 ได้)
     * ส่วนสนามต่างกันในเวลาเดียวกัน จะไม่ถือว่าทับ (จองได้)
     */
    public boolean overlapsWith(int otherCourtNumber, LocalDate otherDate,
                                LocalTime otherStart, LocalTime otherEnd) {
        if (court.getCourtNumber() != otherCourtNumber) {
            return false;
        }
        if (!bookingDate.equals(otherDate)) {
            return false;
        }
        return startTime.isBefore(otherEnd) && otherStart.isBefore(getEndTime());
    }

    /**
     * เลื่อนการจอง: เปลี่ยนสนาม วันที่ และเวลาเริ่ม (จำนวนชั่วโมงเท่าเดิม ค่าสนามจึงไม่เปลี่ยน)
     * เรียกโดย BookingService.updateBooking() หลังตรวจเวลาทำการและสนามว่างแล้วเท่านั้น
     */
    public void reschedule(Court newCourt, LocalDate newDate, LocalTime newStartTime) {
        this.court = newCourt;
        this.bookingDate = newDate;
        this.startTime = newStartTime;
    }

    /** บันทึกว่าชำระค่าสนามแล้ว (เรียกโดย PaymentService เท่านั้น) */
    public void markCourtFeePaid(String slipReference) {
        this.slipReference = slipReference;
        this.paymentStatus = STATUS_PAID;
    }

    public boolean isCourtFeePaid() {
        return STATUS_PAID.equals(paymentStatus);
    }

    /** เพิ่มรายการเช่ารองเท้าให้การจองนี้ */
    public void addShoeRental(ShoeRental shoeRental) {
        shoeRentals.add(shoeRental);
    }

    /** รวมค่าเช่ารองเท้าทั้งหมดของการจองนี้ */
    public double getShoeRentalFee() {
        double total = 0;
        for (ShoeRental rental : shoeRentals) {
            total += rental.getRentalFee();
        }
        return total;
    }

    /** ยอดชำระรวมของการจอง = ค่าสนาม + ค่าเช่ารองเท้า */
    public double getTotalAmount() {
        return courtFee + getShoeRentalFee();
    }

    // ---------------- Getter ----------------

    public String getBookingId() {
        return bookingId;
    }

    public Member getMember() {
        return member;
    }

    public Court getCourt() {
        return court;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public double getCourtFee() {
        return courtFee;
    }

    public int getDiscountedHours() {
        return discountedHours;
    }

    public boolean isUsedPackage() {
        return usedPackage;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public String getSlipReference() {
        return slipReference;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public List<ShoeRental> getShoeRentals() {
        return new ArrayList<>(shoeRentals);
    }

    /**
     * สร้างข้อความรายละเอียดการจองแบบเต็ม (คืนค่าเป็น String)
     * แยกการ "สร้างข้อความ" ออกจากการ "แสดงผล" เพื่อให้ใช้ได้ทั้ง Console (printDetails)
     * และหน้าจอ GUI (นำข้อความไปใส่ในกล่องข้อความ) โดยไม่ต้องเขียนซ้ำ
     */
    public String getDetailsText() {
        StringBuilder text = new StringBuilder();
        text.append("รหัสการจอง    : ").append(bookingId).append("\n");
        text.append("สมาชิก        : ").append(member.getMemberId()).append(" - ").append(member.getFullName()).append("\n");
        text.append("สนาม          : ").append(court.getCourtNumber()).append("\n");
        text.append("วันที่ใช้สนาม  : ").append(FormatHelper.formatDate(bookingDate)).append("\n");
        text.append("เวลา          : ").append(startTime).append(" - ").append(getEndTime())
                .append(" (").append(durationHours).append(" ชั่วโมง)").append("\n");
        String courtFeeNote = "";
        if (usedPackage) {
            courtFeeNote = " (ใช้ชั่วโมงแพ็กเกจ " + durationHours + " ชม.)";
        } else if (discountedHours > 0) {
            courtFeeNote = " (รวมส่วนลด 30% จำนวน " + discountedHours + " ชม.)";
        }
        text.append("ค่าสนาม       : ").append(FormatHelper.money(courtFee)).append(courtFeeNote).append("\n");
        for (ShoeRental rental : shoeRentals) {
            text.append("ค่าเช่ารองเท้า : ").append(FormatHelper.money(rental.getRentalFee()))
                    .append(" (").append(rental.getPairs()).append(" คู่, รหัส ").append(rental.getRentalId()).append(")\n");
        }
        text.append("ยอดรวม        : ").append(FormatHelper.money(getTotalAmount())).append("\n");
        text.append("สลิปค่าสนาม    : ").append(slipReference).append("\n");
        text.append("สถานะ         : ").append(paymentStatus).append("\n");
        text.append("วันที่ทำรายการ : ").append(createdDate);
        return text.toString();
    }

    /** แสดงรายละเอียดการจองแบบเต็มทาง Console */
    public void printDetails() {
        System.out.println(getDetailsText());
    }

    /** แสดงแบบบรรทัดเดียว ใช้ในตารางรายการจอง */
    @Override
    public String toString() {
        return bookingId + " | " + member.getMemberId() + " " + member.getFullName()
                + " | สนาม " + court.getCourtNumber()
                + " | " + bookingDate + " " + startTime + "-" + getEndTime()
                + " | " + durationHours + " ชม."
                + " | " + (usedPackage ? "แพ็กเกจ" : String.format("%,.2f บาท", getTotalAmount()))
                + (usedPackage && getShoeRentalFee() > 0
                    ? String.format(" (+รองเท้า %,.2f บาท)", getShoeRentalFee()) : "");
    }
}
