package com.example.badminton.user;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Member;
import com.example.badminton.model.MembershipPackage;
import com.example.badminton.model.Product;
import com.example.badminton.model.Sale;
import com.example.badminton.model.SaleItem;
import com.example.badminton.model.ShoeRental;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.service.ProductService;
import com.example.badminton.util.FormatHelper;
import com.example.badminton.util.InputHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cashier = พนักงานแคชเชียร์ (สืบทอดจาก SystemUser)
 *
 * หน้าที่ตามโจทย์: บันทึกการจอง, ค้นหาการจอง, แก้ไข/ยกเลิกการจอง, บันทึกการชำระเงินและสลิป,
 *                 รับค่าเช่าสนาม/เช่ารองเท้า/ขายแพ็กเกจ/ขายอุปกรณ์
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - Cashier "ไม่ได้คำนวณหรือเก็บข้อมูลเอง" แต่รับข้อมูลจากหน้าจอ แล้วส่งต่อให้ Service ทำงาน
 *   (BookingService, PaymentService, ProductService, MemberService)
 * - การแยกแบบนี้ทำให้กฎทางธุรกิจ (ราคา ส่วนลด เวลาทำการ) อยู่ใน Service ที่เดียว
 */
public class Cashier extends SystemUser {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final ProductService productService;

    public Cashier(String userId, String name, MemberService memberService, BookingService bookingService,
                   PaymentService paymentService, ProductService productService) {
        super(userId, name, memberService); // เรียก constructor ของคลาสแม่ SystemUser
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.productService = productService;
    }

    /** Override เมธอด abstract จาก SystemUser */
    @Override
    public String getRoleName() {
        return "พนักงานแคชเชียร์";
    }

    /** Override: เมนูของแคชเชียร์ (ต่างจากเมนูของผู้ดูแลระบบ) */
    @Override
    public void showMenu() {
        printWelcome();
        while (true) {
            System.out.println("\n========== เมนูพนักงานแคชเชียร์ ==========");
            System.out.println("1. จองสนามแบดมินตัน");
            System.out.println("2. ดูตารางการจองสนามตามวันที่");
            System.out.println("3. ค้นหาข้อมูลการจอง");
            System.out.println("4. ซื้อแพ็กเกจชั่วโมง (15 ชม. 2,600 บาท)");
            System.out.println("5. ตรวจสอบชั่วโมงแพ็กเกจคงเหลือ");
            System.out.println("6. เช่ารองเท้า (เพิ่มให้การจองที่มีอยู่)");
            System.out.println("7. ขายอุปกรณ์แบดมินตัน");
            System.out.println("8. ดูข้อมูลสมาชิก");
            System.out.println("9. แก้ไข/ยกเลิกการจอง");
            System.out.println("0. กลับเมนูหลัก");
            int choice = InputHelper.readInt("เลือกเมนู: ", 0, 9);
            switch (choice) {
                case 1 -> bookCourt();
                case 2 -> viewCourtSchedule();
                case 3 -> searchBooking();
                case 4 -> buyPackage();
                case 5 -> checkPackageHours();
                case 6 -> rentShoes();
                case 7 -> sellEquipment();
                case 8 -> viewMemberInfo();
                case 9 -> editOrCancelBooking();
                case 0 -> {
                    return; // ออกจากเมนูแคชเชียร์ กลับไปเมนูหลัก
                }
            }
        }
    }

    // =====================================================================
    // 1. จองสนาม
    // =====================================================================

    /**
     * ขั้นตอนการจองสนาม:
     * 1) ตรวจว่าเป็นสมาชิก
     * 2) รับหมายเลขสนาม วันที่ เวลาเริ่ม จำนวนชั่วโมง
     * 3) ตรวจเวลาทำการและสนามว่างด้วย BookingService.validateBooking()
     * 4) เลือกใช้แพ็กเกจ (ถ้ามีชั่วโมงเหลือ) หรือจ่ายเงินปกติ (คำนวณส่วนลดชั่วโมงที่ 20 อัตโนมัติ)
     * 5) ถามการเช่ารองเท้า แสดงสรุปยอด ให้ยืนยัน และกรอกเลขสลิป
     * 6) บันทึกการจอง -> บันทึกการชำระค่าสนาม -> บันทึกการเช่ารองเท้า
     */
    private void bookCourt() {
        printSection("จองสนามแบดมินตัน");
        Member member = askForMember();
        if (member == null) {
            return; // ไม่ใช่สมาชิก -> จองไม่ได้
        }

        int courtNumber = InputHelper.readInt("เลือกหมายเลขสนาม (1-" + BookingService.TOTAL_COURTS + "): ",
                1, BookingService.TOTAL_COURTS);
        LocalDate date = InputHelper.readDate("วันที่ใช้สนาม (ค.ศ. ปี-เดือน-วัน เช่น 2026-10-12): ");
        System.out.println("  วัน" + FormatHelper.thaiDayName(date)
                + (bookingService.isPublicHoliday(date) ? " (วันหยุดนักขัตฤกษ์)" : "")
                + " เปิดให้บริการ " + bookingService.getOpeningTime(date)
                + " - " + bookingService.getClosingTime(date));
        int startHour = InputHelper.readInt("เวลาเริ่ม (ใส่ชั่วโมง 0-23 เช่น 13 = 13:00): ", 0, 23);
        int hours = InputHelper.readInt("จำนวนชั่วโมงที่ต้องการจอง (1-12): ", 1, 12);

        // ตรวจเวลาทำการ / ห้ามย้อนหลัง / สนามว่าง
        String error = bookingService.validateBooking(courtNumber, date, startHour, hours);
        if (error != null) {
            System.out.println("  [จองไม่สำเร็จ] " + error);
            return;
        }
        System.out.println("  สนาม " + courtNumber + " ว่าง สามารถจองได้");

        // เลือกวิธีชำระค่าสนาม: ใช้แพ็กเกจ หรือ จ่ายปกติ
        boolean usePackage = false;
        int remainingHours = member.getRemainingPackageHours();
        if (remainingHours > 0) {
            System.out.println("  สมาชิกมีชั่วโมงแพ็กเกจคงเหลือ " + remainingHours + " ชั่วโมง");
            usePackage = InputHelper.readYesNo("ต้องการใช้ชั่วโมงแพ็กเกจจองครั้งนี้หรือไม่ (y/n): ");
            if (usePackage && remainingHours < hours) {
                System.out.println("  [จองไม่สำเร็จ] ชั่วโมงแพ็กเกจไม่พอ (เหลือ " + remainingHours
                        + " ชม. แต่ต้องการ " + hours + " ชม.) กรุณาลดจำนวนชั่วโมง หรือชำระแบบปกติ");
                return;
            }
        }

        double courtFee = 0;
        if (usePackage) {
            System.out.println("  ใช้ชั่วโมงแพ็กเกจ " + hours + " ชม. (คงเหลือหลังจอง "
                    + (remainingHours - hours) + " ชม.) ไม่มีค่าสนาม");
        } else {
            courtFee = paymentService.calculateCourtFee(member, hours);
            int discountedHours = paymentService.countDiscountedHours(member, hours);
            System.out.println("  ชั่วโมงสะสมก่อนจอง: " + member.getQualifyingHours() + " ชม.");
            if (discountedHours > 0) {
                System.out.println("  ได้ส่วนลด! ชั่วโมงที่ 20 (สะสม) จ่ายเพียง 30% = "
                        + FormatHelper.money(paymentService.getDiscountedHourPrice())
                        + " จำนวน " + discountedHours + " ชม.");
            }
            System.out.println("  ค่าสนาม: " + FormatHelper.money(courtFee));
        }

        // เช่ารองเท้า (ไม่บังคับ)
        int shoePairs = InputHelper.readInt("จำนวนรองเท้าที่ต้องการเช่า (0 = ไม่เช่า, สูงสุด "
                + ShoeRental.MAX_PAIRS + "): ", 0, ShoeRental.MAX_PAIRS);
        double shoeFee = ShoeRental.calculateFee(shoePairs, 1);

        // สรุปยอดก่อนยืนยัน
        double total = courtFee + shoeFee;
        System.out.println("\n  ----- สรุปการจอง -----");
        System.out.println("  สมาชิก : " + member.getMemberId() + " - " + member.getFullName());
        System.out.println("  สนาม " + courtNumber + " วันที่ " + FormatHelper.formatDate(date) + " เวลา "
                + String.format("%02d:00-%02d:00", startHour, startHour + hours) + " (" + hours + " ชม.)");
        System.out.println("  ค่าสนาม        : " + FormatHelper.money(courtFee) + (usePackage ? " (ใช้แพ็กเกจ)" : ""));
        System.out.println("  ค่าเช่ารองเท้า : " + FormatHelper.money(shoeFee) + " (" + shoePairs + " คู่ x 50 บาท)");
        System.out.println("  ยอดชำระรวม    : " + FormatHelper.money(total));
        if (!InputHelper.readYesNo("ยืนยันการจองหรือไม่ (y/n): ")) {
            System.out.println("  ยกเลิกการจองแล้ว");
            return;
        }

        String slip = "-";
        if (total > 0) {
            slip = InputHelper.readText("เลขอ้างอิงสลิป/ที่อยู่ไฟล์สลิป (เช่น slips/0001.jpg หรือ CASH): ");
        }

        try {
            Booking booking = bookingService.createBooking(member, courtNumber, date, startHour, hours, usePackage);
            paymentService.payCourtFee(booking, slip);          // ถ้าใช้แพ็กเกจ จะไม่สร้างรายได้
            if (shoePairs > 0) {
                paymentService.rentShoes(booking, shoePairs, slip);
            }
            System.out.println("\n  จองสำเร็จ! รายละเอียดการจอง:");
            booking.printDetails();
        } catch (IllegalArgumentException e) {
            System.out.println("  [จองไม่สำเร็จ] " + e.getMessage());
        }
    }

    // =====================================================================
    // 2. ดูตารางสนาม
    // =====================================================================

    private void viewCourtSchedule() {
        printSection("ตารางการจองสนาม");
        LocalDate date = InputHelper.readDate("วันที่ (ปี-เดือน-วัน เช่น 2026-10-12): ");
        System.out.println("ตารางสนามวันที่ " + FormatHelper.formatDate(date));
        bookingService.printCourtSchedule(date);
    }

    // =====================================================================
    // 3. ค้นหาการจอง
    // =====================================================================

    private void searchBooking() {
        printSection("ค้นหาข้อมูลการจอง");
        System.out.println("1. ค้นหาด้วยรหัสการจอง");
        System.out.println("2. ค้นหาด้วยรหัสสมาชิก");
        System.out.println("3. ค้นหาด้วยชื่อสมาชิก");
        System.out.println("4. ค้นหาด้วยวันที่ใช้สนาม");
        int choice = InputHelper.readInt("เลือกวิธีค้นหา: ", 1, 4);

        if (choice == 1) {
            String bookingId = InputHelper.readText("รหัสการจอง (เช่น B001): ");
            Booking booking = bookingService.findById(bookingId);
            if (booking == null) {
                System.out.println("  ไม่พบการจองรหัส " + bookingId);
            } else {
                booking.printDetails();
            }
            return;
        }

        List<Booking> result;
        if (choice == 2) {
            result = bookingService.searchByMemberId(InputHelper.readText("รหัสสมาชิก (เช่น M001): "));
        } else if (choice == 3) {
            result = bookingService.searchByMemberName(InputHelper.readText("ชื่อสมาชิก (บางส่วนก็ได้): "));
        } else {
            result = bookingService.searchByDate(InputHelper.readDate("วันที่ (ปี-เดือน-วัน): "));
        }
        printBookingList(result);
        if (!result.isEmpty() && InputHelper.readYesNo("ต้องการดูรายละเอียดการจองรายการใดหรือไม่ (y/n): ")) {
            Booking booking = bookingService.findById(InputHelper.readText("รหัสการจอง: "));
            if (booking == null) {
                System.out.println("  ไม่พบการจองรหัสนี้");
            } else {
                booking.printDetails();
            }
        }
    }

    private void printBookingList(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("  ไม่พบข้อมูลการจอง");
            return;
        }
        System.out.println("  พบ " + bookings.size() + " รายการ:");
        for (Booking booking : bookings) {
            System.out.println("  " + booking);
        }
    }

    // =====================================================================
    // 9. แก้ไข / ยกเลิกการจอง
    // =====================================================================

    /**
     * แก้ไข = เลื่อนสนาม/วันที่/เวลาเริ่ม (จำนวนชั่วโมงและค่าสนามคงเดิม)
     * ยกเลิก = ลบการจอง คืนชั่วโมงแพ็กเกจหรือลดชั่วโมงสะสม และยกเลิกรายการรับเงิน (คืนเงิน)
     * กติกาทั้งหมดอยู่ใน BookingService (updateBooking / cancelBooking)
     */
    private void editOrCancelBooking() {
        printSection("แก้ไข/ยกเลิกการจอง");
        String bookingId = InputHelper.readText("รหัสการจอง (เช่น B001): ");
        Booking booking = bookingService.findById(bookingId);
        String error = bookingService.checkModifiable(booking);
        if (error != null) {
            System.out.println("  [ผิดพลาด] " + (booking == null ? "ไม่พบการจองรหัส " + bookingId : error));
            return;
        }
        booking.printDetails();
        System.out.println("1. แก้ไขสนาม/วันที่/เวลา");
        System.out.println("2. ยกเลิกการจอง");
        System.out.println("0. กลับ");
        int choice = InputHelper.readInt("เลือก: ", 0, 2);
        if (choice == 1) {
            editBooking(booking);
        } else if (choice == 2) {
            cancelBooking(booking);
        }
    }

    private void editBooking(Booking booking) {
        System.out.println("  (จำนวนชั่วโมงคงเดิม " + booking.getDurationHours()
                + " ชม. ถ้าต้องการเปลี่ยนจำนวนชั่วโมง ให้ยกเลิกแล้วจองใหม่)");
        int courtNumber = InputHelper.readInt("สนามใหม่ (1-" + BookingService.TOTAL_COURTS + "): ",
                1, BookingService.TOTAL_COURTS);
        LocalDate date = InputHelper.readDate("วันที่ใหม่ (ปี-เดือน-วัน): ");
        System.out.println("  วัน" + FormatHelper.thaiDayName(date) + " เปิดให้บริการ "
                + bookingService.getOpeningTime(date) + " - " + bookingService.getClosingTime(date));
        int startHour = InputHelper.readInt("เวลาเริ่มใหม่ (ใส่ชั่วโมง 0-23): ", 0, 23);
        String error = bookingService.validateUpdate(booking, courtNumber, date, startHour);
        if (error != null) {
            System.out.println("  [แก้ไขไม่สำเร็จ] " + error);
            return;
        }
        if (!InputHelper.readYesNo(String.format("ยืนยันเลื่อนเป็นสนาม %d วันที่ %s เวลา %02d:00-%02d:00 (y/n): ",
                courtNumber, date, startHour, startHour + booking.getDurationHours()))) {
            System.out.println("  ไม่ได้แก้ไขการจอง");
            return;
        }
        bookingService.updateBooking(booking, courtNumber, date, startHour);
        System.out.println("  แก้ไขการจองสำเร็จ");
        booking.printDetails();
    }

    private void cancelBooking(Booking booking) {
        if (booking.isUsedPackage()) {
            System.out.println("  จะคืนชั่วโมงแพ็กเกจ " + booking.getDurationHours() + " ชม. ให้สมาชิก");
        }
        System.out.println("  ยอดเงินที่ต้องคืน: " + FormatHelper.money(booking.getTotalAmount()));
        if (!InputHelper.readYesNo("ยืนยันยกเลิกการจอง " + booking.getBookingId() + " (y/n): ")) {
            System.out.println("  ไม่ได้ยกเลิกการจอง");
            return;
        }
        double refund = bookingService.cancelBooking(booking);
        System.out.println("  ยกเลิกการจอง " + booking.getBookingId() + " สำเร็จ คืนเงินสมาชิก "
                + FormatHelper.money(refund));
    }

    // =====================================================================
    // 4-5. แพ็กเกจชั่วโมง
    // =====================================================================

    private void buyPackage() {
        printSection("ซื้อแพ็กเกจชั่วโมง");
        Member member = askForMember();
        if (member == null) {
            return;
        }
        if (member.getFreePackagesAvailable() > 0) { // มีสิทธิ์ -> รับฟรี ไม่ต้องใช้สลิป
            System.out.println("  สมาชิกมีสิทธิ์รับแพ็กเกจฟรี " + member.getFreePackagesAvailable() + " สิทธิ์");
            if (InputHelper.readYesNo("ยืนยันรับแพ็กเกจ " + MembershipPackage.PACKAGE_HOURS
                    + " ชั่วโมงฟรี (y/n): ")) {
                System.out.println("  รับแพ็กเกจฟรีสำเร็จ: " + paymentService.claimFreePackage(member));
            }
            return;
        }
        System.out.println("  แพ็กเกจ " + MembershipPackage.PACKAGE_HOURS + " ชั่วโมง ราคา "
                + FormatHelper.money(MembershipPackage.PACKAGE_PRICE));
        System.out.println("  ชั่วโมงคงเหลือปัจจุบัน: " + member.getRemainingPackageHours() + " ชั่วโมง");
        if (!InputHelper.readYesNo("ยืนยันการซื้อแพ็กเกจหรือไม่ (y/n): ")) {
            System.out.println("  ยกเลิกการซื้อแพ็กเกจ");
            return;
        }
        String slip = InputHelper.readText("เลขอ้างอิงสลิป/ที่อยู่ไฟล์สลิป: ");
        try {
            MembershipPackage newPackage = paymentService.sellPackage(member, slip);
            System.out.println("  ซื้อแพ็กเกจสำเร็จ: " + newPackage);
            System.out.println("  ชั่วโมงแพ็กเกจคงเหลือรวม: " + member.getRemainingPackageHours() + " ชั่วโมง");
        } catch (IllegalArgumentException e) {
            System.out.println("  [ผิดพลาด] " + e.getMessage());
        }
    }

    private void checkPackageHours() {
        printSection("ตรวจสอบชั่วโมงแพ็กเกจ");
        Member member = askForMember();
        if (member == null) {
            return;
        }
        List<MembershipPackage> packages = member.getPackages();
        if (packages.isEmpty()) {
            System.out.println("  สมาชิกยังไม่เคยซื้อแพ็กเกจ");
            return;
        }
        for (MembershipPackage p : packages) {
            System.out.println("  " + p);
        }
        System.out.println("  ชั่วโมงแพ็กเกจคงเหลือรวม: " + member.getRemainingPackageHours() + " ชั่วโมง");
    }

    // =====================================================================
    // 6. เช่ารองเท้า
    // =====================================================================

    /** เช่ารองเท้าเพิ่มให้การจองที่บันทึกไว้แล้ว (ค่าเช่า 50 บาท/คู่/วันที่จอง) */
    private void rentShoes() {
        printSection("เช่ารองเท้าแบดมินตัน");
        String bookingId = InputHelper.readText("รหัสการจองที่ต้องการเช่ารองเท้า (เช่น B001): ");
        Booking booking = bookingService.findById(bookingId);
        if (booking == null) {
            System.out.println("  [ผิดพลาด] ไม่พบการจองรหัส " + bookingId + " (ต้องจองสนามก่อนจึงจะเช่ารองเท้าได้)");
            return;
        }
        System.out.println("  การจอง: " + booking);
        int pairs = InputHelper.readInt("จำนวนรองเท้าที่ต้องการเช่า (1-" + ShoeRental.MAX_PAIRS + " คู่): ",
                1, ShoeRental.MAX_PAIRS);
        double fee = ShoeRental.calculateFee(pairs, 1);
        System.out.println("  ค่าเช่า: " + pairs + " คู่ x 1 วัน x 50 บาท = " + FormatHelper.money(fee));
        if (!InputHelper.readYesNo("ยืนยันการเช่าหรือไม่ (y/n): ")) {
            System.out.println("  ยกเลิกการเช่ารองเท้า");
            return;
        }
        String slip = InputHelper.readText("เลขอ้างอิงสลิป/ที่อยู่ไฟล์สลิป: ");
        try {
            ShoeRental rental = paymentService.rentShoes(booking, pairs, slip);
            System.out.println("  เช่ารองเท้าสำเร็จ: " + rental);
            System.out.println("  ยอดรวมของการจอง " + booking.getBookingId() + " ตอนนี้: "
                    + FormatHelper.money(booking.getTotalAmount()));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ผิดพลาด] " + e.getMessage());
        }
    }

    // =====================================================================
    // 7. ขายอุปกรณ์
    // =====================================================================

    /**
     * ขายอุปกรณ์: เลือกสินค้าทีละรายการ ใส่จำนวน ระบบคำนวณราคารวมให้
     * (การ "ซื้อรองเท้า" ทำที่เมนูนี้ แยกจาก "เช่ารองเท้า" ในเมนู 6)
     */
    private void sellEquipment() {
        printSection("ขายอุปกรณ์แบดมินตัน");
        System.out.println("รายการสินค้า:");
        for (Product product : productService.getAllProducts()) {
            System.out.println("  " + product);
        }

        // ลูกค้าจะเป็นสมาชิกหรือไม่ก็ได้
        Member member = null;
        String memberId = InputHelper.readLine("รหัสสมาชิก (กด Enter ถ้าเป็นลูกค้าทั่วไป): ");
        if (!memberId.isEmpty()) {
            member = memberService.findById(memberId);
            if (member == null) {
                System.out.println("  ไม่พบสมาชิก บันทึกเป็นลูกค้าทั่วไป");
            }
        }

        // ตะกร้าสินค้า: วนเพิ่มสินค้าจนกว่าจะพิมพ์ 0
        List<SaleItem> cart = new ArrayList<>();
        while (true) {
            String productId = InputHelper.readText("รหัสสินค้า (เช่น P01, พิมพ์ 0 เมื่อเลือกครบ): ");
            if (productId.equals("0")) {
                break;
            }
            Product product = productService.findProductById(productId);
            if (product == null) {
                System.out.println("  [ผิดพลาด] ไม่พบสินค้ารหัส " + productId);
                continue;
            }
            int quantity = InputHelper.readInt("จำนวน " + product.getName() + " (1-100): ", 1, 100);
            SaleItem item = new SaleItem(product, quantity);
            cart.add(item);
            System.out.println("  เพิ่มลงตะกร้า: " + item);
        }

        if (cart.isEmpty()) {
            System.out.println("  ไม่มีสินค้าในตะกร้า ยกเลิกการขาย");
            return;
        }

        double total = 0;
        System.out.println("\n  ----- สรุปรายการสินค้า -----");
        for (SaleItem item : cart) {
            System.out.println("  " + item);
            total += item.getTotalPrice();
        }
        System.out.println("  ยอดรวม: " + FormatHelper.money(total));
        if (!InputHelper.readYesNo("ยืนยันการขายหรือไม่ (y/n): ")) {
            System.out.println("  ยกเลิกการขาย");
            return;
        }
        String slip = InputHelper.readText("เลขอ้างอิงสลิป/ที่อยู่ไฟล์สลิป (หรือ CASH): ");
        try {
            Sale sale = productService.createSale(member, cart, slip);
            System.out.println("  ขายสำเร็จ!");
            sale.printReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("  [ผิดพลาด] " + e.getMessage());
        }
    }

    // =====================================================================
    // 8. ดูข้อมูลสมาชิก
    // =====================================================================

    private void viewMemberInfo() {
        printSection("ดูข้อมูลสมาชิก");
        System.out.println("1. ค้นหาด้วยรหัสสมาชิก");
        System.out.println("2. ค้นหาด้วยชื่อ");
        int choice = InputHelper.readInt("เลือก: ", 1, 2);
        if (choice == 1) {
            Member member = askForMember();
            if (member != null) {
                member.printInfo();
            }
        } else {
            List<Member> members = memberService.searchByName(InputHelper.readText("ชื่อ (บางส่วนก็ได้): "));
            if (members.isEmpty()) {
                System.out.println("  ไม่พบสมาชิก");
            }
            for (Member member : members) {
                System.out.println();
                member.printInfo();
            }
        }
    }
}
