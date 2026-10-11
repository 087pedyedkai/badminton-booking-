package com.example.badminton.user;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Member;
import com.example.badminton.model.Payment;
import com.example.badminton.model.Product;
import com.example.badminton.report.Reportable;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.service.ProductService;
import com.example.badminton.service.ReportService;
import com.example.badminton.util.InputHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Administrator = ผู้ดูแลระบบ (สืบทอดจาก SystemUser)
 *
 * หน้าที่ตามโจทย์: ดูประวัติการจองของสมาชิก, ดูรายงานรายได้รายวัน/รายเดือน,
 *                 ตรวจสอบข้อมูลการจองทั้งหมดในระบบ
 * เพิ่มเติม: ดูรายการรับเงินทั้งหมด, ดูรายชื่อสมาชิก, จัดการสินค้า, จัดการวันหยุดนักขัตฤกษ์
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - ใช้ ReportService สร้างและแสดงรายงาน (ได้ออบเจกต์ชนิด Reportable กลับมา)
 * - ใช้ BookingService / PaymentService / ProductService อ่านและจัดการข้อมูล
 */
public class Administrator extends SystemUser {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final ReportService reportService;

    public Administrator(String userId, String name, MemberService memberService, BookingService bookingService,
                         PaymentService paymentService, ProductService productService, ReportService reportService) {
        super(userId, name, memberService);
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.productService = productService;
        this.reportService = reportService;
    }

    @Override
    public String getRoleName() {
        return "ผู้ดูแลระบบ";
    }

    /** Override: เมนูของผู้ดูแลระบบ */
    @Override
    public void showMenu() {
        printWelcome();
        while (true) {
            System.out.println("\n========== เมนูผู้ดูแลระบบ ==========");
            System.out.println("1. ดูประวัติการจองของสมาชิก");
            System.out.println("2. ดูรายงานรายได้รายวัน");
            System.out.println("3. ดูรายงานรายได้รายเดือน");
            System.out.println("4. ดูข้อมูลการจองทั้งหมด");
            System.out.println("5. ดูรายการรับเงินทั้งหมด");
            System.out.println("6. ดูรายชื่อสมาชิกทั้งหมด");
            System.out.println("7. จัดการสินค้า (ดู/เพิ่ม/แก้ราคา)");
            System.out.println("8. จัดการวันหยุดนักขัตฤกษ์");
            System.out.println("0. กลับเมนูหลัก");
            int choice = InputHelper.readInt("เลือกเมนู: ", 0, 8);
            switch (choice) {
                case 1 -> viewMemberHistory();
                case 2 -> viewDailyReport();
                case 3 -> viewMonthlyReport();
                case 4 -> viewAllBookings();
                case 5 -> viewAllPayments();
                case 6 -> viewAllMembers();
                case 7 -> manageProducts();
                case 8 -> manageHolidays();
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void viewMemberHistory() {
        printSection("ประวัติการจองของสมาชิก");
        Member member = askForMember();
        if (member == null) {
            return;
        }
        // ตัวแปรชนิด interface (Reportable) เก็บออบเจกต์ MemberHistoryReport
        Reportable report = reportService.createMemberHistoryReport(member);
        reportService.showReport(report);
    }

    private void viewDailyReport() {
        printSection("รายงานรายได้รายวัน");
        LocalDate date = InputHelper.readDate("เลือกวันที่ (ปี-เดือน-วัน เช่น 2026-10-11): ");
        Reportable report = reportService.createDailyReport(date);
        reportService.showReport(report);
    }

    private void viewMonthlyReport() {
        printSection("รายงานรายได้รายเดือน");
        YearMonth yearMonth = InputHelper.readYearMonth("เลือกเดือนและปี (ปี-เดือน เช่น 2026-10): ");
        Reportable report = reportService.createMonthlyReport(yearMonth);
        reportService.showReport(report);
    }

    private void viewAllBookings() {
        printSection("ข้อมูลการจองทั้งหมด");
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) {
            System.out.println("  ยังไม่มีการจอง");
            return;
        }
        for (Booking booking : bookings) {
            System.out.println("  " + booking + " | " + booking.getPaymentStatus());
        }
        System.out.println("  รวม " + bookings.size() + " รายการ");
    }

    private void viewAllPayments() {
        printSection("รายการรับเงินทั้งหมด");
        List<Payment> payments = paymentService.getAllPayments();
        if (payments.isEmpty()) {
            System.out.println("  ยังไม่มีรายการรับเงิน");
            return;
        }
        for (Payment payment : payments) {
            System.out.println("  " + payment);
        }
    }

    private void viewAllMembers() {
        printSection("รายชื่อสมาชิกทั้งหมด");
        for (Member member : memberService.getAllMembers()) {
            System.out.println("  " + member + " | ชั่วโมงสะสม " + member.getQualifyingHours()
                    + " ชม. | แพ็กเกจคงเหลือ " + member.getRemainingPackageHours() + " ชม.");
        }
    }

    private void manageProducts() {
        printSection("จัดการสินค้า");
        for (Product product : productService.getAllProducts()) {
            System.out.println("  " + product);
        }
        System.out.println("1. เพิ่มสินค้าใหม่");
        System.out.println("2. แก้ไขราคาสินค้า");
        System.out.println("0. กลับ");
        int choice = InputHelper.readInt("เลือก: ", 0, 2);
        if (choice == 1) {
            String name = InputHelper.readText("ชื่อสินค้า: ");
            double price = InputHelper.readPositiveDouble("ราคาต่อหน่วย (บาท): ");
            Product product = productService.addProduct(name, price);
            System.out.println("  เพิ่มสินค้าสำเร็จ: " + product);
        } else if (choice == 2) {
            Product product = productService.findProductById(InputHelper.readText("รหัสสินค้า: "));
            if (product == null) {
                System.out.println("  [ผิดพลาด] ไม่พบสินค้า");
                return;
            }
            product.setUnitPrice(InputHelper.readPositiveDouble("ราคาใหม่ (บาท): "));
            System.out.println("  แก้ไขราคาสำเร็จ: " + product);
        }
    }

    private void manageHolidays() {
        printSection("วันหยุดนักขัตฤกษ์ (เปิด 10:00-20:00)");
        for (LocalDate date : bookingService.getPublicHolidays()) {
            System.out.println("  " + date);
        }
        if (InputHelper.readYesNo("ต้องการเพิ่มวันหยุดหรือไม่ (y/n): ")) {
            LocalDate date = InputHelper.readDate("วันที่ (ปี-เดือน-วัน): ");
            if (bookingService.addPublicHoliday(date)) {
                System.out.println("  เพิ่มวันหยุด " + date + " เรียบร้อย");
            } else {
                System.out.println("  วันที่นี้เป็นวันหยุดอยู่แล้ว");
            }
        }
    }
}
