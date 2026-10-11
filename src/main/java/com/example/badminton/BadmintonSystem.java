package com.example.badminton;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Member;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.service.ProductService;
import com.example.badminton.service.ReportService;
import com.example.badminton.user.Administrator;
import com.example.badminton.user.Cashier;
import com.example.badminton.user.SystemUser;

import java.time.LocalDate;

/**
 * BadmintonSystem = "กล่องรวม" ของทั้งระบบ: สร้าง Service ทุกตัว ผู้ใช้ระบบ และข้อมูลตัวอย่าง
 *
 * ทำไมต้องมีคลาสนี้?
 * โปรแกรมมี 2 หน้าตา คือ แบบ Console (Main) และแบบหน้าต่าง GUI (gui.BadmintonApp)
 * ทั้งสองแบบต้องใช้ Service ชุดเดียวกันและข้อมูลตัวอย่างเหมือนกัน
 * จึงรวมการสร้างไว้ที่นี่ที่เดียว แล้วให้ทั้งสองหน้าตาเรียกใช้ -> ไม่ต้องเขียนโค้ดซ้ำ
 *
 * ภาพรวมการเชื่อมต่อ:
 *   PaymentService  <-- BookingService, ProductService, ReportService ใช้งาน
 *   MemberService
 *   Cashier / Administrator (สืบทอดจาก SystemUser) ใช้ Service เหล่านี้
 *
 * ข้อมูลทั้งหมดเก็บในหน่วยความจำ (ArrayList) เมื่อปิดโปรแกรมข้อมูลจะหายไป
 */
public class BadmintonSystem {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final BookingService bookingService;
    private final ProductService productService;
    private final ReportService reportService;
    private final SystemUser cashier;
    private final SystemUser administrator;

    public BadmintonSystem() {
        // ลำดับสำคัญ: PaymentService ต้องถูกสร้างก่อน เพราะ Service อื่นต้องใช้
        paymentService = new PaymentService();
        memberService = new MemberService();
        bookingService = new BookingService(paymentService);
        productService = new ProductService(paymentService);
        reportService = new ReportService(paymentService, bookingService);

        // ประกาศเป็นชนิด SystemUser (คลาสแม่) แต่เก็บออบเจกต์คลาสลูก -> Polymorphism
        // (จำลองการเข้าสู่ระบบ: เลือกบทบาทได้เลย ไม่มีรหัสผ่าน เพราะเป็นโปรแกรมเพื่อการเรียนรู้)
        cashier = new Cashier("C001", "สมหญิง ขยันขาย",
                memberService, bookingService, paymentService, productService);
        administrator = new Administrator("A001", "สมศักดิ์ ดูแลดี",
                memberService, bookingService, paymentService, productService, reportService);

        loadSampleData();
    }

    /**
     * สร้างข้อมูลตัวอย่างสำหรับทดสอบ
     * - วันหยุดนักขัตฤกษ์ปี 2026-2027 (ตัวอย่าง ผู้ดูแลระบบเพิ่มเองได้)
     * - สมาชิก 3 คน
     * - สินค้า 8 รายการ (กำหนดราคาเองตามที่โจทย์อนุญาต)
     * - M002 มีประวัติจองแบบจ่ายเงินปกติแล้ว 18 ชั่วโมง -> ใช้ทดสอบส่วนลดชั่วโมงที่ 20 ได้ทันที
     * - M003 ซื้อแพ็กเกจ 15 ชั่วโมงไว้แล้ว -> ใช้ทดสอบการหักชั่วโมงแพ็กเกจ
     * หมายเหตุ: รายการตัวอย่างที่มีการรับเงิน จะถูกนับเป็นรายได้ของ "วันที่เปิดโปรแกรม"
     *           (ค่าสนาม 18 ชม. x 180 = 3,240 บาท + แพ็กเกจ 2,600 บาท = 5,840 บาท)
     */
    private void loadSampleData() {
        String[] holidays = {
                "2026-01-01", "2026-03-03", "2026-04-06", "2026-04-13", "2026-04-14", "2026-04-15",
                "2026-05-01", "2026-05-04", "2026-06-01", "2026-06-03", "2026-07-28", "2026-07-29",
                "2026-07-30", "2026-08-12", "2026-10-13", "2026-10-23", "2026-12-05", "2026-12-07",
                "2026-12-10", "2026-12-31", "2027-01-01"
        };
        for (String holiday : holidays) {
            bookingService.addPublicHoliday(LocalDate.parse(holiday));
        }

        memberService.registerMember("สมชาย ใจดี", "0812345678");
        Member m2 = memberService.registerMember("สุดา รักกีฬา", "0898765432");
        Member m3 = memberService.registerMember("วิชัย ตีแรง", "0861112222");

        productService.addProduct("ไม้แบดมินตัน รุ่นมาตรฐาน", 1200);
        productService.addProduct("ไม้แบดมินตัน รุ่นโปร", 3500);
        productService.addProduct("ลูกขนไก่ (หลอด 12 ลูก)", 650);
        productService.addProduct("ลูกขนไก่ (ลูกเดี่ยว)", 60);
        productService.addProduct("รองเท้าแบดมินตัน (ขาย)", 1800);
        productService.addProduct("กริปพันด้าม", 80);
        productService.addProduct("เอ็นขึ้นไม้แบด", 250);
        productService.addProduct("ถุงเท้ากีฬา", 120);

        // M002 จองสนาม 12 จำนวน 3 ครั้ง ครั้งละ 6 ชม. (13:00-19:00 อยู่ในเวลาทำการทุกวัน) รวม 18 ชม.
        for (int i = 7; i <= 9; i++) {
            Booking booking = bookingService.createBooking(m2, 12, LocalDate.now().plusDays(i), 13, 6, false);
            paymentService.payCourtFee(booking, "slips/sample-" + booking.getBookingId() + ".jpg");
        }

        // M003 ซื้อแพ็กเกจไว้ 1 แพ็กเกจ
        paymentService.sellPackage(m3, "slips/sample-package.jpg");
    }

    // ---------------- Getter ให้หน้าจอ (Console / GUI) ดึง Service ไปใช้ ----------------

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public MemberService getMemberService() {
        return memberService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public SystemUser getCashier() {
        return cashier;
    }

    public SystemUser getAdministrator() {
        return administrator;
    }
}
