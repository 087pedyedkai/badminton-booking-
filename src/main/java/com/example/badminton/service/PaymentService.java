package com.example.badminton.service;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Court;
import com.example.badminton.model.Member;
import com.example.badminton.model.MembershipPackage;
import com.example.badminton.model.Payment;
import com.example.badminton.model.PaymentType;
import com.example.badminton.model.ShoeRental;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PaymentService ดูแลเรื่อง "เงิน" ทั้งหมด:
 * - คำนวณค่าสนาม (รวมกติกาส่วนลดชั่วโมงที่ 20)
 * - บันทึกการรับเงินทุกครั้ง (Payment) พร้อมสลิป
 * - ขายแพ็กเกจชั่วโมง และให้เช่ารองเท้า
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - BookingService ใช้ calculateCourtFee() คำนวณค่าสนามก่อนสร้าง Booking
 * - ProductService ใช้ recordPayment() บันทึกรายได้จากการขายอุปกรณ์
 * - ReportService อ่าน getAllPayments() ไปสรุปรายได้รายวัน/รายเดือน
 *
 * กติกาป้องกันการนับรายได้ซ้ำ:
 * Payment ทุกรายการต้องอ้างอิงรายการต้นทาง (referenceId) และ
 * "หมวด + รายการต้นทางเดียวกัน" จะบันทึกได้เพียงครั้งเดียว (ดู recordPayment)
 */
public class PaymentService {

    /*
     * ---------------- กติกาส่วนลดชั่วโมงที่ 20 ----------------
     * โจทย์: เมื่อสมาชิกจองครบ 19 ชั่วโมงแล้ว ชั่วโมงที่ 20 จ่ายเพียง 30% ของราคาปกติ
     *        = 180 x 30 / 100 = 54 บาท
     *
     * วิธีคิดในโปรแกรม:
     * - Member มีตัวนับ qualifyingHours (ชั่วโมงสะสมที่จ่ายเงินแบบปกติ)
     * - เวลาจอง n ชั่วโมง ให้นับชั่วโมงทีละชั่วโมงต่อจากชั่วโมงสะสมเดิม
     *   เช่น สะสมแล้ว 18 ชม. แล้วจองเพิ่ม 3 ชม. -> ชั่วโมงที่ 19, 20, 21
     *   ชั่วโมงที่ 20 คิด 54 บาท ที่เหลือคิด 180 บาท -> 180 + 54 + 180 = 414 บาท
     * - หลังบันทึกการจองสำเร็จ ตัวนับจะเพิ่มขึ้น (18 -> 21) ชั่วโมงที่ 20 จึงถูกใช้ไปแล้ว
     *   และจะไม่ได้ส่วนลดซ้ำอีก (ป้องกันการให้ส่วนลดซ้ำกับชั่วโมงเดิม)
     * - กติกาที่เลือก: ให้ส่วนลดทุก ๆ รอบ 20 ชั่วโมง (ชั่วโมงที่ 20, 40, 60, ...)
     *   ถ้าต้องการให้ได้ส่วนลด "ครั้งเดียว" ให้แก้ isDiscountHour() เป็น hourNumber == 20
     * - ชั่วโมงที่ใช้แพ็กเกจ "ไม่นับ" เป็นชั่วโมงสะสม (แพ็กเกจเป็นราคาพิเศษอยู่แล้ว)
     */
    public static final int DISCOUNT_HOUR_INTERVAL = 20; // ทุก ๆ ชั่วโมงที่ 20
    public static final int DISCOUNT_PERCENT_TO_PAY = 30; // จ่าย 30% ของราคาปกติ

    private final List<Payment> payments = new ArrayList<>();
    private final List<ShoeRental> shoeRentals = new ArrayList<>();
    private int nextPaymentNumber = 1;
    private int nextPackageNumber = 1;
    private int nextRentalNumber = 1;

    // ---------------- การคำนวณค่าสนาม ----------------

    /** ชั่วโมงสะสมลำดับที่ hourNumber ได้ส่วนลดหรือไม่ */
    public boolean isDiscountHour(int hourNumber) {
        return hourNumber % DISCOUNT_HOUR_INTERVAL == 0;
    }

    /** ราคาชั่วโมงที่ได้ส่วนลด = 180 x 30 / 100 = 54 บาท */
    public double getDiscountedHourPrice() {
        return Court.STANDARD_PRICE_PER_HOUR * DISCOUNT_PERCENT_TO_PAY / 100.0;
    }

    /**
     * คำนวณค่าสนามของการจอง n ชั่วโมง (แบบจ่ายเงินปกติ ไม่ใช้แพ็กเกจ)
     * ขั้นตอน: วนทีละชั่วโมง -> หาลำดับชั่วโมงสะสม (ชั่วโมงสะสมเดิม + i)
     *          -> ถ้าเป็นชั่วโมงที่ได้ส่วนลด คิด 54 บาท ไม่เช่นนั้นคิด 180 บาท
     * หมายเหตุ: เมธอดนี้ "แค่คำนวณ" ยังไม่เปลี่ยนชั่วโมงสะสมของสมาชิก
     */
    public double calculateCourtFee(Member member, int hours) {
        double total = 0;
        int hoursBefore = member.getQualifyingHours();
        for (int i = 1; i <= hours; i++) {
            int hourNumber = hoursBefore + i;
            if (isDiscountHour(hourNumber)) {
                total += getDiscountedHourPrice();
            } else {
                total += Court.STANDARD_PRICE_PER_HOUR;
            }
        }
        return total;
    }

    /** นับว่าการจอง n ชั่วโมงนี้ มีกี่ชั่วโมงที่ได้ส่วนลด (ใช้แสดงผลให้แคชเชียร์เห็น) */
    public int countDiscountedHours(Member member, int hours) {
        int count = 0;
        int hoursBefore = member.getQualifyingHours();
        for (int i = 1; i <= hours; i++) {
            if (isDiscountHour(hoursBefore + i)) {
                count++;
            }
        }
        return count;
    }

    // ---------------- การบันทึกรับเงิน ----------------

    /**
     * บันทึกการรับเงิน 1 ครั้ง (ทุกช่องทางรายได้ต้องผ่านเมธอดนี้)
     * ขั้นตอน:
     * 1) ตรวจว่ารายการต้นทางนี้ (หมวดเดียวกัน) ยังไม่เคยถูกบันทึกรับเงิน -> กันนับซ้ำ
     * 2) สร้าง Payment (constructor ตรวจว่าจำนวนเงินต้องมากกว่า 0)
     * 3) เก็บลง List เพื่อให้รายงานรายได้นำไปสรุป
     */
    public Payment recordPayment(PaymentType type, double amount, Member member,
                                 String referenceId, String slipReference) {
        if (isAlreadyPaid(type, referenceId)) {
            throw new IllegalArgumentException("รายการ " + referenceId + " ถูกบันทึกรับเงินไปแล้ว (ห้ามบันทึกซ้ำ)");
        }
        String paymentId = String.format("P%04d", nextPaymentNumber);
        Payment payment = new Payment(paymentId, LocalDate.now(), type, amount, member, referenceId, slipReference);
        payments.add(payment);
        if (member != null) {
            member.addPaidAmount(amount); // สะสมยอดชำระเพื่อสิทธิ์แพ็กเกจฟรี
        }
        nextPaymentNumber++;
        return payment;
    }

    /** ตรวจว่ามี Payment หมวดนี้ของรายการต้นทางนี้อยู่แล้วหรือไม่ */
    public boolean isAlreadyPaid(PaymentType type, String referenceId) {
        for (Payment payment : payments) {
            if (payment.getType() == type && payment.getReferenceId().equals(referenceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * รับชำระค่าสนามของการจอง
     * - ถ้าจองด้วยแพ็กเกจ หรือค่าสนามเป็น 0 -> ไม่สร้าง Payment (ไม่มีเงินเข้าจริง)
     * - ถ้ามีค่าสนาม -> บันทึก Payment หมวด "ค่าเช่าสนาม" แล้วเปลี่ยนสถานะการจองเป็นชำระแล้ว
     */
    public Payment payCourtFee(Booking booking, String slipReference) {
        if (booking.isUsedPackage() || booking.getCourtFee() <= 0) {
            return null;
        }
        Payment payment = recordPayment(PaymentType.COURT_RENTAL, booking.getCourtFee(),
                booking.getMember(), booking.getBookingId(), slipReference);
        booking.markCourtFeePaid(slipReference);
        return payment;
    }

    // ---------------- แพ็กเกจชั่วโมง ----------------

    /**
     * ขายแพ็กเกจ 15 ชั่วโมง ราคา 2,600 บาท
     * ขั้นตอน: สร้าง MembershipPackage -> ผูกกับสมาชิก -> บันทึกรายได้หมวด "ขายแพ็กเกจ"
     * รายได้ของแพ็กเกจถูกนับ "ครั้งเดียวตรงนี้" ตอนใช้ชั่วโมงภายหลังจะไม่นับเป็นรายได้อีก
     */
    public MembershipPackage sellPackage(Member member, String slipReference) {
        if (member == null) {
            throw new IllegalArgumentException("ต้องเป็นสมาชิกจึงจะซื้อแพ็กเกจได้");
        }
        String packageId = String.format("PK%03d", nextPackageNumber);
        MembershipPackage membershipPackage = new MembershipPackage(packageId, LocalDate.now());
        recordPayment(PaymentType.PACKAGE_SALE, MembershipPackage.PACKAGE_PRICE, member, packageId, slipReference);
        member.addPackage(membershipPackage);
        nextPackageNumber++;
        return membershipPackage;
    }

    /**
     * รับแพ็กเกจ 15 ชั่วโมงฟรีด้วยสิทธิ์ที่สะสมไว้ (ไม่บันทึก Payment เพราะไม่มีเงินเข้า)
     */
    public MembershipPackage claimFreePackage(Member member) {
        if (member == null || member.getFreePackagesAvailable() <= 0) {
            throw new IllegalArgumentException("ยังไม่มีสิทธิ์รับแพ็กเกจฟรี");
        }
        String packageId = String.format("PK%03d", nextPackageNumber);
        MembershipPackage membershipPackage = new MembershipPackage(packageId, LocalDate.now());
        member.addPackage(membershipPackage);
        member.useFreePackageRight();
        nextPackageNumber++;
        return membershipPackage;
    }

    // ---------------- เช่ารองเท้า ----------------

    /**
     * ให้เช่ารองเท้าสำหรับการจอง 1 รายการ (การจอง 1 รายการ = ใช้สนาม 1 วัน)
     * ค่าเช่า = จำนวนคู่ x 1 วัน x 50 บาท และบันทึกรายได้หมวด "ค่าเช่ารองเท้า"
     * (แยกจาก "ขายรองเท้า" ซึ่งเป็นสินค้าใน ProductService)
     */
    public ShoeRental rentShoes(Booking booking, int pairs, String slipReference) {
        if (booking == null) {
            throw new IllegalArgumentException("ไม่พบการจอง ต้องเช่ารองเท้าคู่กับการจองสนาม");
        }
        String rentalId = String.format("R%03d", nextRentalNumber);
        ShoeRental rental = new ShoeRental(rentalId, booking, pairs, 1, slipReference);
        recordPayment(PaymentType.SHOE_RENTAL, rental.getRentalFee(), booking.getMember(), rentalId, slipReference);
        booking.addShoeRental(rental);
        shoeRentals.add(rental);
        nextRentalNumber++;
        return rental;
    }

    // ---------------- ยกเลิกการจอง ----------------

    /**
     * ยกเลิกรายการเงินทั้งหมดของการจอง (เรียกโดย BookingService.cancelBooking())
     * กติกาที่เลือก: ยกเลิกการจอง = คืนเงินเต็มจำนวน จึงลบ Payment ค่าสนามและค่าเช่ารองเท้าของการจองนี้
     * ออกจากระบบ รายงานรายได้จะไม่นับเงินก้อนนี้อีก (รายได้นับเฉพาะเงินที่ร้านได้รับจริง)
     * คืนยอดเงินที่ต้องคืนให้สมาชิก
     */
    public double cancelBookingPayments(Booking booking) {
        List<String> referenceIds = new ArrayList<>();
        referenceIds.add(booking.getBookingId());
        for (ShoeRental rental : booking.getShoeRentals()) {
            referenceIds.add(rental.getRentalId());
            shoeRentals.remove(rental);
        }
        double refund = 0;
        for (int i = payments.size() - 1; i >= 0; i--) {
            Payment payment = payments.get(i);
            boolean ofThisBooking = (payment.getType() == PaymentType.COURT_RENTAL
                    || payment.getType() == PaymentType.SHOE_RENTAL)
                    && referenceIds.contains(payment.getReferenceId());
            if (ofThisBooking) {
                refund += payment.getAmount();
                payment.getMember().removePaidAmount(payment.getAmount());
                payments.remove(i);
            }
        }
        return refund;
    }

    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments);
    }

    public List<ShoeRental> getAllShoeRentals() {
        return new ArrayList<>(shoeRentals);
    }
}
