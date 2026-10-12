package com.example.badminton.service;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Court;
import com.example.badminton.model.Member;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingService ดูแลเรื่อง "การจองสนาม" ทั้งหมด:
 * - เก็บสนาม 12 สนาม และรายการจองทั้งหมด
 * - ตรวจเวลาเปิด-ปิด (จ-ศ 13:00-21:00 / ส-อา และวันหยุดนักขัตฤกษ์ 10:00-20:00)
 * - ตรวจการจองซ้อน (สนามเดียวกัน วันเดียวกัน เวลาทับกัน = ห้าม)
 * - สร้างการจอง หักชั่วโมงแพ็กเกจ / เพิ่มชั่วโมงสะสม
 * - แก้ไข (เลื่อนสนาม/วัน/เวลา) และยกเลิกการจอง พร้อมคืนชั่วโมงแพ็กเกจหรือคืนเงิน
 * - ค้นหาการจองตามรหัสการจอง รหัสสมาชิก ชื่อสมาชิก หรือวันที่
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - ใช้ PaymentService คำนวณค่าสนาม (ส่งเข้ามาทาง constructor)
 * - Cashier เรียก validateBooking() และ createBooking() ตอนจองสนาม
 * - Administrator เรียก getAllBookings() เพื่อตรวจดูรายการจองทั้งหมด
 */
public class BookingService {

    public static final int TOTAL_COURTS = 12;

    // เวลาเปิด-ปิดตามโจทย์
    private static final LocalTime WEEKDAY_OPEN = LocalTime.of(13, 0);
    private static final LocalTime WEEKDAY_CLOSE = LocalTime.of(21, 0);
    private static final LocalTime WEEKEND_OPEN = LocalTime.of(10, 0);
    private static final LocalTime WEEKEND_CLOSE = LocalTime.of(20, 0);

    private final List<Court> courts = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<LocalDate> publicHolidays = new ArrayList<>();
    private final PaymentService paymentService;
    private int nextBookingNumber = 1;

    public BookingService(PaymentService paymentService) {
        this.paymentService = paymentService;
        // สร้างสนามที่ 1 ถึง 12
        for (int i = 1; i <= TOTAL_COURTS; i++) {
            courts.add(new Court(i));
        }
    }

    // ---------------- วันหยุดและเวลาทำการ ----------------

    /** เพิ่มวันหยุดนักขัตฤกษ์ (ผู้ดูแลระบบเพิ่มได้จากเมนู) */
    public boolean addPublicHoliday(LocalDate date) {
        if (publicHolidays.contains(date)) {
            return false;
        }
        publicHolidays.add(date);
        return true;
    }

    public List<LocalDate> getPublicHolidays() {
        return new ArrayList<>(publicHolidays);
    }

    public boolean isPublicHoliday(LocalDate date) {
        return publicHolidays.contains(date);
    }

    /** เสาร์ อาทิตย์ หรือวันหยุดนักขัตฤกษ์ ใช้เวลาทำการแบบวันหยุด */
    public boolean isWeekendOrHoliday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || isPublicHoliday(date);
    }

    public LocalTime getOpeningTime(LocalDate date) {
        return isWeekendOrHoliday(date) ? WEEKEND_OPEN : WEEKDAY_OPEN;
    }

    public LocalTime getClosingTime(LocalDate date) {
        return isWeekendOrHoliday(date) ? WEEKEND_CLOSE : WEEKDAY_CLOSE;
    }

    // ---------------- สนาม ----------------

    /** คืน Court ตามหมายเลข หรือ null ถ้าหมายเลขไม่ถูกต้อง */
    public Court getCourt(int courtNumber) {
        for (Court court : courts) {
            if (court.getCourtNumber() == courtNumber) {
                return court;
            }
        }
        return null;
    }

    /** ตรวจว่าสนามว่างในช่วงเวลาที่ต้องการหรือไม่ (ไม่ทับกับการจองใดเลย) */
    public boolean isCourtAvailable(int courtNumber, LocalDate date, LocalTime start, LocalTime end) {
        return isCourtAvailable(courtNumber, date, start, end, null);
    }

    /**
     * เหมือนด้านบน แต่ไม่นับการจอง ignored (ใช้ตอนแก้ไขการจอง: เลื่อนเวลาไปทับเวลาเดิมของตัวเองได้)
     */
    private boolean isCourtAvailable(int courtNumber, LocalDate date, LocalTime start, LocalTime end,
                                     Booking ignored) {
        for (Booking booking : bookings) {
            if (booking != ignored && booking.overlapsWith(courtNumber, date, start, end)) {
                return false;
            }
        }
        return true;
    }

    // ---------------- การตรวจสอบและสร้างการจอง ----------------

    /**
     * ตรวจสอบข้อมูลการจองทั้งหมด คืนข้อความ error ภาษาไทย หรือคืน null ถ้าผ่านทุกข้อ
     * ลำดับการตรวจ:
     * 1) หมายเลขสนามต้องอยู่ระหว่าง 1-12
     * 2) วันที่ต้องไม่เป็นวันที่ผ่านมาแล้ว
     * 3) จำนวนชั่วโมงต้องมากกว่า 0
     * 4) เวลาเริ่มต้องไม่ก่อนเวลาเปิด และเวลาสิ้นสุดต้องไม่เกินเวลาปิดของวันนั้น
     * 5) สนามต้องว่าง (ไม่ทับกับการจองอื่นของสนามเดียวกัน)
     */
    public String validateBooking(int courtNumber, LocalDate date, int startHour, int hours) {
        return validate(courtNumber, date, startHour, hours, null);
    }

    /** ตรวจเหมือน validateBooking() โดยไม่นับการจอง ignored ว่าเป็นการจองที่ทับเวลา */
    private String validate(int courtNumber, LocalDate date, int startHour, int hours, Booking ignored) {
        if (getCourt(courtNumber) == null) {
            return "หมายเลขสนามไม่ถูกต้อง (มีสนามที่ 1 ถึง " + TOTAL_COURTS + ")";
        }
        if (date == null || date.isBefore(LocalDate.now())) {
            return "ไม่สามารถจองย้อนหลังได้ (วันที่ต้องเป็นวันนี้หรืออนาคต)";
        }
        if (hours <= 0) {
            return "จำนวนชั่วโมงต้องมากกว่า 0";
        }
        LocalTime open = getOpeningTime(date);
        LocalTime close = getClosingTime(date);
        // ตรวจด้วยตัวเลขชั่วโมงก่อน (กันกรณีเวลาเกินเที่ยงคืนแล้ววนกลับเป็นตอนเช้า)
        if (startHour < open.getHour() || startHour + hours > close.getHour()) {
            return "อยู่นอกเวลาทำการ วันนี้เปิด " + open + " - " + close
                    + " (คุณขอ " + String.format("%02d:00", startHour)
                    + " - " + String.format("%02d:00", startHour + hours) + ")";
        }
        LocalTime start = LocalTime.of(startHour, 0);
        LocalTime end = start.plusHours(hours);
        if (!isCourtAvailable(courtNumber, date, start, end, ignored)) {
            return "สนาม " + courtNumber + " ถูกจองแล้วในช่วงเวลา " + start + " - " + end
                    + " ของวันที่ " + date + " กรุณาเลือกเวลาหรือสนามอื่น";
        }
        return null;
    }

    /**
     * สร้างและบันทึกการจอง
     * ขั้นตอน:
     * 1) ต้องเป็นสมาชิก (member ไม่เป็น null)
     * 2) ตรวจข้อมูลด้วย validateBooking() อีกครั้ง (กันข้อมูลเปลี่ยนระหว่างกรอก)
     * 3) ถ้าใช้แพ็กเกจ: ตรวจชั่วโมงคงเหลือพอหรือไม่ -> ค่าสนาม = 0
     *    ถ้าจ่ายปกติ:   ให้ PaymentService คำนวณค่าสนาม (รวมส่วนลดชั่วโมงที่ 20)
     * 4) สร้าง Booking
     * 5) หักชั่วโมงแพ็กเกจ หรือ เพิ่มชั่วโมงสะสม (ทำหลังคำนวณราคาแล้ว เพื่อไม่ให้ส่วนลดคลาดเคลื่อน)
     * 6) เก็บ Booking ไว้ในระบบ และในประวัติของสมาชิก
     * การรับเงินค่าสนามจะทำต่อโดย PaymentService.payCourtFee()
     */
    public Booking createBooking(Member member, int courtNumber, LocalDate date,
                                 int startHour, int hours, boolean usePackage) {
        if (member == null) {
            throw new IllegalArgumentException("ไม่พบสมาชิก เฉพาะสมาชิกเท่านั้นที่จองสนามได้");
        }
        String error = validateBooking(courtNumber, date, startHour, hours);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        double courtFee = 0;
        int discountedHours = 0;
        if (usePackage) {
            if (member.getRemainingPackageHours() < hours) {
                throw new IllegalArgumentException("ชั่วโมงแพ็กเกจไม่พอ (เหลือ "
                        + member.getRemainingPackageHours() + " ชม. แต่ต้องการ " + hours + " ชม.)");
            }
        } else {
            courtFee = paymentService.calculateCourtFee(member, hours);
            discountedHours = paymentService.countDiscountedHours(member, hours);
        }

        String bookingId = String.format("B%03d", nextBookingNumber);
        Booking booking = new Booking(bookingId, member, getCourt(courtNumber), date,
                LocalTime.of(startHour, 0), hours, courtFee, discountedHours, usePackage);

        if (usePackage) {
            member.usePackageHours(hours);      // หักชั่วโมงแพ็กเกจ (ไม่มีรายได้ใหม่)
        } else {
            member.addQualifyingHours(hours);   // นับชั่วโมงสะสมเพื่อส่วนลดครั้งต่อไป
        }

        bookings.add(booking);
        member.addBooking(booking);
        nextBookingNumber++;
        return booking;
    }

    // ---------------- แก้ไข / ยกเลิกการจอง ----------------

    /**
     * ตรวจว่าการจองนี้ยังแก้ไขหรือยกเลิกได้หรือไม่ คืนข้อความ error หรือ null ถ้าทำได้
     * กติกา: การจองของวันที่ผ่านมาแล้ว (ใช้สนามไปแล้ว) แก้ไข/ยกเลิกไม่ได้
     */
    public String checkModifiable(Booking booking) {
        if (booking == null || !bookings.contains(booking)) {
            return "ไม่พบการจองนี้ในระบบ";
        }
        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            return "การจอง " + booking.getBookingId() + " เป็นของวันที่ผ่านมาแล้ว แก้ไขหรือยกเลิกไม่ได้";
        }
        return null;
    }

    /**
     * ตรวจข้อมูลการแก้ไขการจอง (สนาม/วันที่/เวลาเริ่มใหม่) คืนข้อความ error หรือ null ถ้าผ่าน
     * จำนวนชั่วโมงคงเดิม (ค่าสนาม ส่วนลด และชั่วโมงแพ็กเกจจึงไม่เปลี่ยน)
     * ถ้าต้องการเปลี่ยนจำนวนชั่วโมง ให้ยกเลิกการจองเดิมแล้วจองใหม่
     */
    public String validateUpdate(Booking booking, int courtNumber, LocalDate date, int startHour) {
        String error = checkModifiable(booking);
        if (error != null) {
            return error;
        }
        return validate(courtNumber, date, startHour, booking.getDurationHours(), booking);
    }

    /**
     * แก้ไขการจอง: เลื่อนไปสนาม/วันที่/เวลาใหม่
     * ขั้นตอน: ตรวจด้วย validateUpdate() (ไม่นับเวลาเดิมของการจองนี้ว่าทับ) -> Booking.reschedule()
     */
    public void updateBooking(Booking booking, int courtNumber, LocalDate date, int startHour) {
        String error = validateUpdate(booking, courtNumber, date, startHour);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        booking.reschedule(getCourt(courtNumber), date, LocalTime.of(startHour, 0));
    }

    /**
     * ยกเลิก (ลบ) การจอง ทำย้อนกลับทุกอย่างที่ createBooking() และการชำระเงินทำไว้:
     * 1) ใช้แพ็กเกจ -> คืนชั่วโมงแพ็กเกจ / จ่ายปกติ -> ลดชั่วโมงสะสม (ส่วนลดชั่วโมงที่ 20 ไม่คลาดเคลื่อน)
     * 2) ให้ PaymentService ยกเลิกรายการรับเงินค่าสนามและค่าเช่ารองเท้า (คืนเงิน)
     * 3) ลบการจองออกจากระบบและจากประวัติของสมาชิก
     * คืนยอดเงินที่ต้องคืนให้สมาชิก (0 ถ้าใช้แพ็กเกจและไม่ได้เช่ารองเท้า)
     */
    public double cancelBooking(Booking booking) {
        String error = checkModifiable(booking);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        Member member = booking.getMember();
        if (booking.isUsedPackage()) {
            member.returnPackageHours(booking.getDurationHours());
        } else {
            member.removeQualifyingHours(booking.getDurationHours());
        }
        double refund = paymentService.cancelBookingPayments(booking);
        bookings.remove(booking);
        member.removeBooking(booking);
        return refund;
    }

    // ---------------- การค้นหา ----------------

    public Booking findById(String bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId.trim())) {
                return booking;
            }
        }
        return null;
    }

    public List<Booking> searchByMemberId(String memberId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getMember().getMemberId().equalsIgnoreCase(memberId.trim())) {
                result.add(booking);
            }
        }
        return result;
    }

    /** ค้นหาจากชื่อสมาชิก (พิมพ์บางส่วนของชื่อก็ได้) */
    public List<Booking> searchByMemberName(String keyword) {
        List<Booking> result = new ArrayList<>();
        String lowerKeyword = keyword.trim().toLowerCase();
        for (Booking booking : bookings) {
            if (booking.getMember().getFullName().toLowerCase().contains(lowerKeyword)) {
                result.add(booking);
            }
        }
        return result;
    }

    /** ค้นหาการจองตามวันที่ใช้สนาม */
    public List<Booking> searchByDate(LocalDate date) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getBookingDate().equals(date)) {
                result.add(booking);
            }
        }
        return result;
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    /** แสดงตารางการจองของทุกสนามในวันที่เลือก เพื่อช่วยแคชเชียร์เลือกสนามที่ว่าง */
    public void printCourtSchedule(LocalDate date) {
        System.out.println("เวลาทำการ: " + getOpeningTime(date) + " - " + getClosingTime(date)
                + (isPublicHoliday(date) ? " (วันหยุดนักขัตฤกษ์)" : ""));
        for (Court court : courts) {
            StringBuilder line = new StringBuilder(String.format("สนาม %2d : ", court.getCourtNumber()));
            boolean hasBooking = false;
            for (Booking booking : bookings) {
                if (booking.getCourt() == court && booking.getBookingDate().equals(date)) {
                    line.append("[").append(booking.getStartTime()).append("-")
                            .append(booking.getEndTime()).append(" ").append(booking.getBookingId()).append("] ");
                    hasBooking = true;
                }
            }
            if (!hasBooking) {
                line.append("ว่างทั้งวัน");
            }
            System.out.println(line);
        }
    }
}
